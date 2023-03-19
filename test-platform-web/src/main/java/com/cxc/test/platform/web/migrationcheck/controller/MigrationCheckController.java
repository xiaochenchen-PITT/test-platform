package com.cxc.test.platform.web.migrationcheck.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxc.test.platform.common.domain.AmisResult;
import com.cxc.test.platform.common.domain.FeatureKeyConstant;
import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.common.domain.diff.DiffResult;
import com.cxc.test.platform.common.domain.diff.TaskStatusEnum;
import com.cxc.test.platform.common.utils.CommonUtils;
import com.cxc.test.platform.common.utils.ErrorMessageUtils;
import com.cxc.test.platform.common.utils.ExcelUtils;
import com.cxc.test.platform.infra.config.DatabaseConfig;
import com.cxc.test.platform.infra.domain.diff.DiffDetailPO;
import com.cxc.test.platform.infra.domain.diff.DiffResultPO;
import com.cxc.test.platform.infra.domain.migrationcheck.MappingRulePO;
import com.cxc.test.platform.infra.domain.migrationcheck.MigrationConfigPO;
import com.cxc.test.platform.infra.domain.migrationcheck.SourceInitSqlPO;
import com.cxc.test.platform.infra.domain.migrationcheck.SourceLocatorPO;
import com.cxc.test.platform.infra.mapper.master.*;
import com.cxc.test.platform.migrationcheck.domain.CustomizedMethod;
import com.cxc.test.platform.migrationcheck.domain.SourceLocator;
import com.cxc.test.platform.migrationcheck.domain.config.MigrationCheckConfig;
import com.cxc.test.platform.migrationcheck.domain.config.MigrationConfig;
import com.cxc.test.platform.migrationcheck.domain.mapping.MappingRule;
import com.cxc.test.platform.migrationcheck.domain.mapping.SourceMappingItem;
import com.cxc.test.platform.migrationcheck.domain.mapping.TargetMappingItem;
import com.cxc.test.platform.migrationcheck.service.MigrationCheckService;
import com.cxc.test.platform.migrationcheck.service.MigrationConfigService;
import com.cxc.test.platform.web.BaseController;
import com.cxc.test.platform.web.migrationcheck.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

// TODO: 2023/3/8 断点续跑

@Slf4j
@CrossOrigin
@Controller
@RequestMapping(value = "/migrationcheck")
public class MigrationCheckController extends BaseController {

    @Autowired
    @Qualifier("migrationCheckService")
    MigrationCheckService migrationCheckService;
//    DemoMigrationCheckService migrationCheckService;

    @Resource
    MigrationConfigService migrationConfigService;

    @Resource
    MigrationConfigMapper migrationConfigMapper;

    @Resource
    MappingRuleMapper mappingRuleMapper;

    @Resource
    SourceLocatorMapper sourceLocatorMapper;

    @Resource
    SourceInitSqlMapper sourceInitSqlMapper;

    @Resource
    DiffResultMapper diffResultMapper;

    @Resource
    DiffDetailMapper diffDetailMapper;

    public final ExecutorService singleExecutorService = Executors.newFixedThreadPool(1);

    private final String CONFIG_TABLE_SPLIT = "@";
    private final String CONFIG_FIELD_SPLIT = ",";

    @GetMapping("/set")
    public String set() {
        return "migrationcheck/configSet";
    }

    @GetMapping("/manage")
    public String manage() {
        return "migrationcheck/configManage";
    }

    @GetMapping("/detail")
    public String detail() {
        return "migrationcheck/configDetail";
    }

    @GetMapping("/diff")
    public String diff() {
        return "migrationcheck/diffDetail";
    }

    @RequestMapping(value = "/trigger", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult trigger(@RequestParam Long configId) {
        try {
            if (migrationCheckService.isRunning()) {
                return AmisResult.fail("当前机器已有任务在运行，请等待其结束之后再触发", null);
            }

            String triggerUrl = buildTriggerUrl();

            ResultDO<MigrationConfig> queryRet = migrationConfigService.getConfig(configId);
            if (!queryRet.getIsSuccess()) {
                return AmisResult.fail("没有找到配置，config id: " + configId, null);
            }

            MigrationConfig migrationConfig = queryRet.getData();
            MigrationCheckConfig migrationCheckConfig = MigrationCheckConfig.newInstance(true);
            Long batchId = System.currentTimeMillis();
            singleExecutorService.submit(() -> {
                ResultDO<DiffResult> ret = migrationCheckService.compare(batchId, configId, migrationConfig, migrationCheckConfig, triggerUrl);
            });

            return AmisResult.simpleSuccess("success", "触发成功");
        } catch (Exception e) {
            log.error("failed to trigger compare", e);
            return AmisResult.fail(ErrorMessageUtils.getMessage(e), null);
        }
    }

    @RequestMapping(value = "/stop", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult stop() {
        try {
            String triggerUrl = buildTriggerUrl();

            boolean ret = migrationCheckService.stop();
            if (ret) {
                return AmisResult.simpleSuccess("success", "停止成功");
            } else {
                return AmisResult.fail("停止失败", null);
            }

        } catch (Exception e) {
            log.error("failed to stop compare", e);
            return AmisResult.fail(ErrorMessageUtils.getMessage(e), null);
        }
    }

    private DiffResultPO getLastDiffResult(List<DiffResultPO> diffResultPOList, Long batchIdSearch) {
        DiffResultPO last = null;

        if (CollectionUtils.isEmpty(diffResultPOList)) {
            return last;
        }

        for (DiffResultPO diffResultPO : diffResultPOList) {
            // 指定batchId查询
            if (batchIdSearch != null && batchIdSearch.equals(diffResultPO.getBatchId())) {
                return diffResultPO;
            }

            if (last == null) {
                last = diffResultPO;
            } else {
                if (last.getCreatedTime().compareTo(diffResultPO.getCreatedTime()) < 0) {
                    last = diffResultPO;
                }
            }
        }

        return last;
    }

    private Long parseMappingRuleCount(DiffResultPO diffResultPO) {
        if (diffResultPO == null) {
            return 0L;
        }

        String value = CommonUtils.convterToMap(diffResultPO.getFeatures()).get(FeatureKeyConstant.MAPPING_RULE_COUNT);
        if (StringUtils.isNumeric(value)) {
            return Long.valueOf(value);
        }

        return 0L;
    }

    private boolean filterStatusSearch(String statusSearch, DiffResultPO diffResultPO) {
        if (StringUtils.isEmpty(statusSearch)) {
            return true;
        }

        // 需要单独处理一下not_started，因为这是个默认值，不在diffResultPOList中
        if (diffResultPO == null && TaskStatusEnum.NOT_STARTED.getStatus().equalsIgnoreCase(statusSearch)) {
            return true;
        }

        if (diffResultPO != null && statusSearch.equalsIgnoreCase(diffResultPO.getStatus())) {
            return true;
        }

        return false;
    }

    @RequestMapping(value = "/get_config_list", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getConfigList(@RequestParam(required = false) Long configIdSearch, @RequestParam(required = false) Long batchIdSearch,
                                    @RequestParam(required = false) String statusSearch) {
        String triggerUrl = buildTriggerUrl();

        ResultDO<Map<Long, List<DiffResultPO>>> queryRet = migrationConfigService.getConfigList(configIdSearch);
        if (!queryRet.getIsSuccess()) {
            return AmisResult.emptySuccess();
        }

        List<MigrationRunningItemVO> migrationRunningItemVOList = new ArrayList<>();
        for (Map.Entry<Long, List<DiffResultPO>> entry : queryRet.getData().entrySet()) {
            Long configId = entry.getKey();
            DiffResultPO lastDiffResultPO = getLastDiffResult(entry.getValue(), batchIdSearch);

            if (filterStatusSearch(statusSearch, lastDiffResultPO)) {
                MigrationRunningItemVO migrationRunningItemVO = MigrationRunningItemVO.builder()
                    .batchId(lastDiffResultPO == null ? null : lastDiffResultPO.getBatchId())
                    .configId(configId)
                    .mappingRuleCount(parseMappingRuleCount(lastDiffResultPO))
                    .status(lastDiffResultPO == null ? TaskStatusEnum.NOT_STARTED.getStatus() : lastDiffResultPO.getStatus())
                    .progress(lastDiffResultPO == null ? "0" : lastDiffResultPO.getProgress().substring(0, lastDiffResultPO.getProgress().length() - 1))
                    .runner(lastDiffResultPO == null || StringUtils.isBlank(lastDiffResultPO.getRunner()) ? "" :
                        lastDiffResultPO.getRunner())
                    .totalTaskCount(lastDiffResultPO == null || lastDiffResultPO.getTotalCount() == null ? 0 :
                        lastDiffResultPO.getTotalCount())
                    .failedTaskCount(lastDiffResultPO == null || lastDiffResultPO.getFailedCount() == null ? 0 :
                        lastDiffResultPO.getFailedCount())
                    .createdTime(lastDiffResultPO == null ? "" : CommonUtils.getPrettyDate(lastDiffResultPO.getCreatedTime()))
                    .modifiedTime(lastDiffResultPO == null ? "" : CommonUtils.getPrettyDate(lastDiffResultPO.getModifiedTime()))
                    .build();

                migrationRunningItemVOList.add(migrationRunningItemVO);
            }
        }

        JSONObject retData = new JSONObject();
        retData.put("rows", migrationRunningItemVOList);
        retData.put("count", migrationRunningItemVOList.size());

        return AmisResult.success(retData, "ok");
    }

    @RequestMapping(value = "/add_config", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult addConfig(@RequestBody MigrationConfigVO migrationConfigVO) {
        String triggerUrl = buildTriggerUrl();

        MigrationConfig migrationConfig = convert(migrationConfigVO);

        Assert.isTrue(CollectionUtils.isEqualCollection(migrationConfig.getTableAndInitSqlMap().keySet(), migrationConfig.getRelatedSourceTables()),
            "【源数据初始化】和【字段映射关系】中源表不一致，请检查配置");
        Assert.isTrue(CollectionUtils.isEqualCollection(migrationConfig.getTableAndLocatorMap().keySet(), migrationConfig.getRelatedTargetTables()),
            "【目标表中源数据定位】和【字段映射关系】中目标表不一致，请检查配置");

        ResultDO<Long> ret = migrationConfigService.addConfig(migrationConfig);
        if (ret.getIsSuccess()) {
            return AmisResult.simpleSuccess("success", "保存成功，config id:" + ret.getData());
        } else {
            return AmisResult.fail(ret.getErrorMessage(), null);
        }
    }

    @RequestMapping(value = "/delete_config", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult deleteConfig(@RequestParam Long configId) {
        String triggerUrl = buildTriggerUrl();

        ResultDO<Boolean> ret = migrationConfigService.deleteConfig(configId, false);
        if (ret.getIsSuccess()) {
            return AmisResult.simpleSuccess("success", "删除成功，config id:" + configId);
        } else {
            return AmisResult.fail(ret.getErrorMessage(), null);
        }
    }

    @RequestMapping(value = "/get_migration_db_config", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getMigrationDbConfig(@RequestParam Long configId) {
        String triggerUrl = buildTriggerUrl();

        MigrationConfigPO migrationConfigPO = migrationConfigMapper.getByConfigId(configId);
        if (migrationConfigPO == null) {
            log.info("Did not find config for config id: " + configId);
            return AmisResult.emptySuccess();
        }

        MigrationDbConfigVO migrationDbConfigVO = MigrationDbConfigVO.builder()
            .sourceDriverClassName(migrationConfigPO.getSourceDriverClassName())
            .sourceDbUrl(migrationConfigPO.getSourceDbUrl())
            .sourceUserName(migrationConfigPO.getSourceUserName())
            .sourcePassword(migrationConfigPO.getSourcePassword())
            .targetDriverClassName(migrationConfigPO.getTargetDriverClassName())
            .targetDbUrl(migrationConfigPO.getTargetDbUrl())
            .targetUserName(migrationConfigPO.getTargetUserName())
            .targetPassword(migrationConfigPO.getTargetPassword())
            .build();

        return AmisResult.success((JSONObject) JSONObject.toJSON(migrationDbConfigVO), "ok");
    }

    @RequestMapping(value = "/update_migration_db_config", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult updateMigrationDbConfig(@RequestParam Long configId, @RequestBody MigrationDbConfigVO migrationDbConfigVO) {
        String triggerUrl = buildTriggerUrl();

        MigrationConfigPO migrationConfigPO = new MigrationConfigPO();
        migrationConfigPO.setConfigId(configId);
        migrationConfigPO.setSourceDriverClassName(migrationDbConfigVO.getSourceDriverClassName());
        migrationConfigPO.setSourceDbUrl(migrationDbConfigVO.getSourceDbUrl());
        migrationConfigPO.setSourceUserName(migrationDbConfigVO.getSourceUserName());
        migrationConfigPO.setSourcePassword(migrationDbConfigVO.getSourcePassword());
        migrationConfigPO.setTargetDriverClassName(migrationDbConfigVO.getTargetDriverClassName());
        migrationConfigPO.setTargetDbUrl(migrationDbConfigVO.getTargetDbUrl());
        migrationConfigPO.setTargetUserName(migrationDbConfigVO.getTargetUserName());
        migrationConfigPO.setTargetPassword(migrationDbConfigVO.getTargetPassword());

        int ret = migrationConfigMapper.update(migrationConfigPO);
        Assert.isTrue(ret == 1, "update migration config failed");

        return AmisResult.simpleSuccess("success", "编辑成功");
    }

    @RequestMapping(value = "/get_mapping_rule_config", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getMappingRuleConfig(@RequestParam Long configId, @RequestParam(required = false) String sourceTableNameSearch,
                                           @RequestParam(required = false) String targetTableNameSearch) {
        String triggerUrl = buildTriggerUrl();

        List<MappingRulePO> ret = mappingRuleMapper.getByConfigId(configId);
        if (CollectionUtils.isEmpty(ret)) {
            log.info("Did not find config for config id: " + configId);
            return AmisResult.emptySuccess();
        }

        JSONObject retData = new JSONObject();
        JSONArray rowJA = new JSONArray();
        for (MappingRulePO mappingRulePO : ret) {
            if (StringUtils.isNotEmpty(sourceTableNameSearch) && StringUtils.isNotEmpty(mappingRulePO.getSourceTableName())
                && !mappingRulePO.getSourceTableName().contains(sourceTableNameSearch)) {
                continue;
            }

            if (StringUtils.isNotEmpty(targetTableNameSearch) && StringUtils.isNotEmpty(mappingRulePO.getTargetTableName())
                && !mappingRulePO.getTargetTableName().contains(targetTableNameSearch)) {
                continue;
            }

            MappingRuleVO mappingRuleVO = MappingRuleVO.builder()
                .id(mappingRulePO.getId())
                .configId(mappingRulePO.getConfigId())
                .sourceTableName(mappingRulePO.getSourceTableName())
                .sourceFieldNames(mappingRulePO.getSourceFieldNames())
                .isPrimaryKey(mappingRulePO.getIsPrimaryKey() == 1 ? Boolean.TRUE : Boolean.FALSE)
                .targetTableName(mappingRulePO.getTargetTableName())
                .targetFieldName(mappingRulePO.getTargetFieldName())
                .fieldCheckMethodName(mappingRulePO.getFieldCheckMethodName())
                .fieldCheckMethodArgs(mappingRulePO.getFieldCheckMethodArgs())
                .build();

            rowJA.add(mappingRuleVO);
        }
        retData.put("rows", rowJA);
        retData.put("count", rowJA.size());

        return AmisResult.success(retData, "ok");
    }

    @RequestMapping(value = "/export_mapping_rule", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult exportMappingRule(@RequestParam Long configId) {
        String triggerUrl = buildTriggerUrl();

        return getMappingRuleConfig(configId, null, null);
    }

    @RequestMapping(value = "/update_mapping_rule", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult updateMappingRuleConfig(@RequestParam Long id, @RequestBody MappingRuleVO mappingRuleVO) {
        String triggerUrl = buildTriggerUrl();

        MappingRulePO mappingRulePO = new MappingRulePO();
        mappingRulePO.setId(id);
        mappingRulePO.setSourceTableName(mappingRuleVO.getSourceTableName());
        mappingRulePO.setSourceFieldNames(mappingRuleVO.getSourceFieldNames());
        mappingRulePO.setIsPrimaryKey(mappingRuleVO.getIsPrimaryKey() ? 1 : 0);
        mappingRulePO.setTargetTableName(mappingRuleVO.getTargetTableName());
        mappingRulePO.setTargetFieldName(mappingRuleVO.getTargetFieldName());
        mappingRulePO.setFieldCheckMethodName(mappingRuleVO.getFieldCheckMethodName());
        mappingRulePO.setFieldCheckMethodArgs(convertArgsFromVO2PO(mappingRuleVO.getFieldCheckMethodArgs()));

        int ret = mappingRuleMapper.update(mappingRulePO);
        Assert.isTrue(ret == 1, "update mapping rule failed");

        return AmisResult.simpleSuccess("success", "编辑成功");
    }

    @RequestMapping(value = "/delete_mapping_rule", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult deleteMappingRuleConfig(@RequestParam Long id) {
        String triggerUrl = buildTriggerUrl();

        MappingRulePO mappingRulePO = mappingRuleMapper.getById(id);
        if (mappingRulePO.getIsPrimaryKey() == 1) {
            return AmisResult.fail("主键primary key为true的不能删除", null);
        }

        int ret = mappingRuleMapper.delete(id);
        Assert.isTrue(ret == 1, "delete mapping rule failed");

        return AmisResult.simpleSuccess("success", "删除成功");
    }

    @RequestMapping(value = "/get_locator_config", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getLocatorConfig(@RequestParam Long configId, @RequestParam(required = false) String targetTableNameSearch) {
        String triggerUrl = buildTriggerUrl();

        List<SourceLocatorPO> ret = sourceLocatorMapper.getByConfigId(configId);
        if (CollectionUtils.isEmpty(ret)) {
            log.info("Did not find config for config id: " + configId);
            return AmisResult.emptySuccess();
        }

        JSONObject retData = new JSONObject();
        JSONArray rowJA = new JSONArray();
        for (SourceLocatorPO sourceLocatorPO : ret) {
            String targetTable = sourceLocatorPO.getTargetTableName();
            if (StringUtils.isNotEmpty(targetTableNameSearch) && StringUtils.isNotEmpty(targetTable)
                && !targetTable.contains(targetTableNameSearch)) {
                continue;
            }

            SourceLocatorVO sourceLocatorVO = SourceLocatorVO.builder()
                .id(sourceLocatorPO.getId())
                .configId(sourceLocatorPO.getConfigId())
                .targetTableName(targetTable)
                .locateField(sourceLocatorPO.getLocateField())
                .locateMethodName(sourceLocatorPO.getLocateMethodName())
                .locateMethodArgs(sourceLocatorPO.getLocateMethodArgs())
                .build();

            rowJA.add(sourceLocatorVO);
        }
        retData.put("rows", rowJA);
        retData.put("count", rowJA.size());

        return AmisResult.success(retData, "ok");
    }

    @RequestMapping(value = "/update_locator", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult updateLocator(@RequestParam Long id, @RequestBody SourceLocatorVO sourceLocatorVO) {
        String triggerUrl = buildTriggerUrl();

        SourceLocatorPO sourceLocatorPO = new SourceLocatorPO();
        sourceLocatorPO.setId(id);
        sourceLocatorPO.setTargetTableName(sourceLocatorVO.getTargetTableName());
        sourceLocatorPO.setLocateField(sourceLocatorVO.getLocateField());
        sourceLocatorPO.setLocateMethodName(sourceLocatorVO.getLocateMethodName());
        sourceLocatorPO.setLocateMethodArgs(convertArgsFromVO2PO(sourceLocatorVO.getLocateMethodArgs()));

        int ret = sourceLocatorMapper.update(sourceLocatorPO);
        Assert.isTrue(ret == 1, "update source locator failed");

        return AmisResult.simpleSuccess("success", "编辑成功");
    }

    @RequestMapping(value = "/get_init_sql_config", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getInitSqlConfig(@RequestParam Long configId, @RequestParam(required = false) String sourceTableNameSearch) {
        String triggerUrl = buildTriggerUrl();

        List<SourceInitSqlPO> ret = sourceInitSqlMapper.getByConfigId(configId);
        if (CollectionUtils.isEmpty(ret)) {
            log.info("Did not find config for config id: " + configId);
            return AmisResult.emptySuccess();
        }

        JSONObject retData = new JSONObject();
        JSONArray rowJA = new JSONArray();
        for (SourceInitSqlPO sourceInitSqlPO : ret) {
            String sourceTable = sourceInitSqlPO.getSourceTableName();
            if (StringUtils.isNotEmpty(sourceTableNameSearch) && StringUtils.isNotEmpty(sourceTable)
                && !sourceTable.contains(sourceTableNameSearch)) {
                continue;
            }

            SourceInitSqlVO sourceInitSqlVO = SourceInitSqlVO.builder()
                .id(sourceInitSqlPO.getId())
                .configId(sourceInitSqlPO.getConfigId())
                .sourceTableName(sourceTable)
                .initSql(sourceInitSqlPO.getInitSql())
                .build();

            rowJA.add(sourceInitSqlVO);
        }
        retData.put("rows", rowJA);
        retData.put("count", rowJA.size());

        return AmisResult.success(retData, "ok");
    }

    @RequestMapping(value = "/update_init_sql", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult updateInitSql(@RequestParam Long id, @RequestBody SourceInitSqlVO sourceInitSqlVO) {
        String triggerUrl = buildTriggerUrl();

        SourceInitSqlPO sourceInitSqlPO = new SourceInitSqlPO();
        sourceInitSqlPO.setId(id);
        sourceInitSqlPO.setSourceTableName(sourceInitSqlVO.getSourceTableName());
        sourceInitSqlPO.setInitSql(sourceInitSqlVO.getInitSql());

        int ret = sourceInitSqlMapper.update(sourceInitSqlPO);
        Assert.isTrue(ret == 1, "update source init sql failed");

        return AmisResult.simpleSuccess("success", "编辑成功");
    }

    @RequestMapping(value = "/get_result", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getResult(@RequestParam Long batchId) {
        String triggerUrl = buildTriggerUrl();

        if (batchId == null) {
            return AmisResult.fail("batchId缺失", null);
        }

        DiffResultPO diffResultPO = diffResultMapper.getByBatchId(batchId);
        if (diffResultPO == null) {
            log.info("Did not find result for batch id: " + batchId);
            return AmisResult.fail("没有找到对比结果，batchId:" + batchId, null);
        }

        DiffResultVO diffResultVO = DiffResultVO.builder()
            .batchId(batchId)
            .configId(diffResultPO.getConfigId())
            .isSuccess(diffResultPO.getIsSuccess() == 1 ? "成功" : "失败")
            .isEqual(diffResultPO.getIsEqual() == 1 ? "一致" : "不一致")
            .status(TaskStatusEnum.getByStatus(diffResultPO.getStatus()).getLabel())
            .progress(diffResultPO.getProgress().substring(0, diffResultPO.getProgress().length() - 1))
            .runner(diffResultPO.getRunner())
            .errorMessage(diffResultPO.getErrorMessage())
            .triggerUrl(diffResultPO.getTriggerUrl())
            .totalCount(diffResultPO.getTotalCount())
            .failedCount(diffResultPO.getFailedCount())
            .build();

        return AmisResult.success((JSONObject) JSONObject.toJSON(diffResultVO), "ok");
    }

    @RequestMapping(value = "/get_detail_list", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getDetailList(@RequestParam Long batchId, @RequestParam(required = false) String diffTypeSearch,
                                    @RequestParam(required = false) String sourceSearch,
                                    @RequestParam(required = false) String targetSearch) {
        String triggerUrl = buildTriggerUrl();

        if (batchId == null) {
            return AmisResult.fail("batchId缺失", null);
        }

        List<DiffDetailPO> detailPOList = diffDetailMapper.getByBatchId(batchId);
        if (CollectionUtils.isEmpty(detailPOList)) {
            log.info("Did not find result for batch id: " + batchId);
            return AmisResult.emptySuccess();
        }

        List<DiffDetailVO> detailVOList = new ArrayList<>();
        for (DiffDetailPO diffDetailPO : detailPOList) {
            if (StringUtils.isNotEmpty(diffTypeSearch) && !diffTypeSearch.equalsIgnoreCase(diffDetailPO.getDiffType())) {
                continue;
            }

            if (StringUtils.isNotEmpty(sourceSearch) && !sourceSearch.equalsIgnoreCase(diffDetailPO.getSourceQuery())) {
                continue;
            }

            if (StringUtils.isNotEmpty(targetSearch) && !targetSearch.equalsIgnoreCase(diffDetailPO.getTargetQuery())) {
                continue;
            }

            DiffDetailVO diffDetailVO = DiffDetailVO.builder()
                .id(diffDetailPO.getId())
                .batchId(diffDetailPO.getBatchId())
                .configId(diffDetailPO.getConfigId())
                .diffType(diffDetailPO.getDiffType())
                .sourceQuery(diffDetailPO.getSourceQuery())
                .sourceTableName(diffDetailPO.getSourceTableName())
                .sourceFieldName(diffDetailPO.getSourceFieldName())
                .sourceValue(diffDetailPO.getSourceValue())
                .computedSourceValue(diffDetailPO.getComputedSourceValue())
                .targetQuery(diffDetailPO.getTargetQuery())
                .targetTableName(diffDetailPO.getTargetTableName())
                .targetFieldName(diffDetailPO.getTargetFieldName())
                .targetValue(diffDetailPO.getTargetValue())
                .errorMessage(diffDetailPO.getErrorMessage())
                .build();

            detailVOList.add(diffDetailVO);
        }

        JSONObject retData = new JSONObject();
        retData.put("rows", detailVOList);
        retData.put("count", detailVOList.size());

        return AmisResult.success(retData, "ok");
    }

    private MigrationConfig convert(MigrationConfigVO migrationConfigVO) {
        MigrationConfig migrationConfig = new MigrationConfig();
        Long configId = System.currentTimeMillis();
        migrationConfig.setConfigId(configId);

        // db链接
        migrationConfig.setSourceDbConfig(DatabaseConfig.builder()
            .driverClassName(trim(migrationConfigVO.getSourceDriverClassName()))
            .url(trim(migrationConfigVO.getSourceDbUrl()))
            .name(trim(migrationConfigVO.getSourceUserName()))
            .pwd(trim(migrationConfigVO.getSourcePassword()))
            .build());

        migrationConfig.setTargetDbConfig(DatabaseConfig.builder()
            .driverClassName(trim(migrationConfigVO.getTargetDriverClassName()))
            .url(trim(migrationConfigVO.getTargetDbUrl()))
            .name(trim(migrationConfigVO.getTargetUserName()))
            .pwd(trim(migrationConfigVO.getTargetPassword()))
            .build());

        // mappingRuleList
        List<MappingRule> mappingRuleList = new ArrayList<>();
        JSONArray mappingRuleJA = migrationConfigVO.getExcel();
        for (Object mappingRuleO : mappingRuleJA) {
            JSONObject mappingRuleJO = new JSONObject((LinkedHashMap) mappingRuleO);

            MappingRule mappingRule = MappingRule.builder()
                .sourceMappingItem(SourceMappingItem.builder()
                    .tableName(trim(mappingRuleJO.getString(MappingRule.EXCEL_SOURCE_TABLE_NAME)))
                    .fieldNames(trim(mappingRuleJO.getString(MappingRule.EXCEL_SOURCE_FIELD_NAME)))
                    .isPrimaryKey(mappingRuleJO.getBooleanValue(MappingRule.EXCEL_IS_PRIMARY_KEY))
                    .build())
                .targetMappingItem(TargetMappingItem.builder()
                    .tableName(trim(mappingRuleJO.getString(MappingRule.EXCEL_TARGET_TABLE_NAME)))
                    .fieldName(trim(mappingRuleJO.getString(MappingRule.EXCEL_TARGET_FIELD_NAME)))
                    .build())
                .fieldCheckMethod(CustomizedMethod.builder()
                    .beanName(trim(mappingRuleJO.getString(MappingRule.EXCEL_FIELD_CHECK_METHOD_NAME)))
                    .args(parseArgFromVO(mappingRuleJO.getString(MappingRule.EXCEL_FIELD_CHECK_METHOD_ARGS)))
                    .build())
                .build();

            mappingRuleList.add(mappingRule);
        }

        migrationConfig.setMappingRuleList(mappingRuleList);

        // tableAndInitSqlMap
        Map<String, String> tableAndInitSqlMap = new HashMap<>();
        Set<String> sourceTableList = migrationConfig.getRelatedSourceTables();
        for (String sourceTable : sourceTableList) {
            // 默认补充select * from tablexxx
            tableAndInitSqlMap.put(sourceTable, "select * from " + sourceTable);
        }

        JSONArray initSqlComboJA = migrationConfigVO.getInitSqlCombo();
        if (initSqlComboJA != null) {
            for (Object initSqlComboO : initSqlComboJA) {
                JSONObject initSqlComboJO = new JSONObject((LinkedHashMap) initSqlComboO);
                // 自定义初始化sql
                tableAndInitSqlMap.put(trim(initSqlComboJO.getString("sourceTableName")), trim(initSqlComboJO.getString("sourceDataSql")));
            }
        }
        migrationConfig.setTableAndInitSqlMap(tableAndInitSqlMap);

        // tableAndLocatorMethodMap
        Map<String, SourceLocator> tableAndLocatorMap = new HashMap<>();
        JSONArray locatorComboJA = migrationConfigVO.getLocatorCombo();
        for (Object locatorComboO : locatorComboJA) {
            JSONObject locatorComboJO = new JSONObject((LinkedHashMap) locatorComboO);
            SourceLocator sourceLocator = SourceLocator.builder()
                .locateField(trim(locatorComboJO.getString("targetLocateField")))
                .locateMethod(CustomizedMethod.builder()
                    .beanName(trim(locatorComboJO.getString("targetLocateMethod")))
                    .args(parseArgFromVO(locatorComboJO.getString("targetLocateMethodArgs")))
                    .build())
                .build();

            tableAndLocatorMap.put(trim(locatorComboJO.getString("targetTableName")), sourceLocator);
        }
        migrationConfig.setTableAndLocatorMap(tableAndLocatorMap);

        return migrationConfig;
    }

    private List<Object> parseArgFromVO(String argsStr) {
        if (StringUtils.isBlank(argsStr)) {
            return null;
        }

        argsStr = argsStr.replace("[", "").replace("]", "");

        if (StringUtils.isBlank(argsStr)) {
            return null;
        }

        return Arrays.stream(argsStr.split(","))
            .map(s -> trim(s))
            .collect(Collectors.toList());
    }

    private String convertArgsFromVO2PO(String argsStr) {
        List<Object> argList = parseArgFromVO(argsStr);

        if (CollectionUtils.isEmpty(argList)) {
            return "";
        }

        return String.valueOf(argList);
    }

    /**********************************************************************************
     * old
     *********************************************************************************/

    public MigrationCheckConfig parseConfig(String config) {
        MigrationCheckConfig migrationCheckConfig = MigrationCheckConfig.newInstance(true);

        // tableAndCheckFieldsMap
        Map<String, Set<String>> map = new HashMap<>();
        if (StringUtils.isNotEmpty(config)) {
            try {
                List<String> tableConfigList = Arrays.asList(config.split(";"));
                for (String tableConfig : tableConfigList) {
                    String tableName = tableConfig.split(CONFIG_TABLE_SPLIT)[0];
                    String fields = tableConfig.split(CONFIG_TABLE_SPLIT)[1];
                    Set<String> fieldSet = new HashSet<>(Arrays.asList(fields.split(CONFIG_FIELD_SPLIT)));
                    map.put(tableName, fieldSet);
                }
            } catch (Exception e) {
                log.info("Invalid config format {}, ignored.", config);
            }
        }

        migrationCheckConfig.setTableAndCheckFieldsMap(map);
        return migrationCheckConfig;
    }

    public String buildSqlLimitClause(Integer limit) {
        return limit != null && limit > 0 ? " limit " + limit + " offset 10" : " limit 3000";
    }

    public List<MappingRule> parseFromExcel(String fileName) {
        List<MappingRule> mappingRuleList = new ArrayList<>();

        List<Map<Integer, String>> dataList = ExcelUtils.read(fileName);
        for (Map<Integer, String> data : dataList) {
            try {
                MappingRule mappingRule = MappingRule.builder()
                    .sourceMappingItem(SourceMappingItem.builder()
                        .tableName(trim(data.get(0)))
                        .fieldNames(trim(data.get(1)))
                        .isPrimaryKey(Boolean.parseBoolean(trim(data.get(2))))
                        .build())
                    .targetMappingItem(TargetMappingItem.builder()
                        .tableName(trim(data.get(3)))
                        .fieldName(trim(data.get(4)))
                        .build())
                    .fieldCheckMethod(CustomizedMethod.builder()
                        .beanName(trim(data.get(5)))
                        .args(parse(data.get(6)))
                        .build())
                    .build();

                mappingRuleList.add(mappingRule);
            } catch (Exception e) {
                log.error(String.format("Failed to parse excel row, data: %s", String.valueOf(data)), e);
                continue;
            }
        }

        return mappingRuleList;
    }

    private static String trim(String input) {
        if (StringUtils.isEmpty(input)) {
            return "";
        }

        return input.trim();
    }

    private static List<Object> parse(String input) {
        if (StringUtils.isEmpty(input)) {
            return new ArrayList<>();
        }

        List<Object> args = new ArrayList<>();
        for (String s : input.split(",")) {
            args.add(s.trim());
        }

        return args;
    }
}
