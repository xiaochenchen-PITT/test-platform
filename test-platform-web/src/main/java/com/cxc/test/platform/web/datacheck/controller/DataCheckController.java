package com.cxc.test.platform.web.datacheck.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxc.test.platform.common.diff.ThreadPoolFactory;
import com.cxc.test.platform.common.domain.AmisResult;
import com.cxc.test.platform.common.domain.FeatureKeyConstant;
import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.common.domain.diff.DiffResult;
import com.cxc.test.platform.common.domain.diff.TaskStatusEnum;
import com.cxc.test.platform.common.utils.CommonUtils;
import com.cxc.test.platform.common.utils.ErrorMessageUtils;
import com.cxc.test.platform.common.utils.ExcelUtils;
import com.cxc.test.platform.datacheck.domain.CustomizedMethod;
import com.cxc.test.platform.datacheck.domain.SourceLocator;
import com.cxc.test.platform.datacheck.domain.config.DataCheckConfig;
import com.cxc.test.platform.datacheck.domain.config.DataConfig;
import com.cxc.test.platform.datacheck.domain.mapping.MappingRule;
import com.cxc.test.platform.datacheck.domain.mapping.SourceMappingItem;
import com.cxc.test.platform.datacheck.domain.mapping.TargetMappingItem;
import com.cxc.test.platform.datacheck.service.DataConfigService;
import com.cxc.test.platform.datacheck.service.DemoDataCheckServiceImpl;
import com.cxc.test.platform.infra.config.DatabaseConfig;
import com.cxc.test.platform.infra.config.MachineUtils;
import com.cxc.test.platform.infra.domain.datacheck.DataConfigPO;
import com.cxc.test.platform.infra.domain.datacheck.MappingRulePO;
import com.cxc.test.platform.infra.domain.datacheck.SourceInitSqlPO;
import com.cxc.test.platform.infra.domain.datacheck.SourceLocatorPO;
import com.cxc.test.platform.infra.domain.diff.DiffDetailPO;
import com.cxc.test.platform.infra.domain.diff.DiffResultPO;
import com.cxc.test.platform.infra.mapper.master.*;
import com.cxc.test.platform.toolcenter.domain.ToolQuery;
import com.cxc.test.platform.web.BaseController;
import com.cxc.test.platform.web.datacheck.vo.*;
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
import java.util.stream.Collectors;

// TODO: 2023/3/8 断点续跑

@Slf4j
@CrossOrigin
@Controller
@RequestMapping(value = "/datacheck")
public class DataCheckController extends BaseController {

    @Autowired
    @Qualifier("demoDataCheckServiceImpl")
//    DataCheckServiceImpl dataCheckService;
    DemoDataCheckServiceImpl dataCheckService;

    @Resource
    DataConfigService dataConfigService;

    @Resource
    DataConfigMapper dataConfigMapper;

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

    // 限制同时最多3个并发
    public final ExecutorService executorService = ThreadPoolFactory.getGeneralExecutorService();

    private final String CONFIG_TABLE_SPLIT = "@";
    private final String CONFIG_FIELD_SPLIT = ",";

    @GetMapping("/set")
    public String set() {
        return "datacheck/configSet";
    }

    @GetMapping("/manage")
    public String manage() {
        return "datacheck/configManage";
    }

    @GetMapping("/detail")
    public String detail() {
        return "datacheck/configDetail";
    }

    @GetMapping("/diff")
    public String diff() {
        return "datacheck/diffDetail";
    }

    @RequestMapping(value = "/add_config", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult addConfig(@RequestBody DataConfigVO dataConfigVO) {
        String triggerUrl = buildTriggerUrl();

        DataConfig dataConfig = convert(dataConfigVO);

        Assert.isTrue(CollectionUtils.isEqualCollection(dataConfig.getTableAndInitSqlMap().keySet(), dataConfig.getRelatedSourceTables()),
            "【源数据初始化】和【字段映射关系】中源表不一致，请检查配置");
        Assert.isTrue(CollectionUtils.isEqualCollection(dataConfig.getTableAndLocatorMap().keySet(), dataConfig.getRelatedTargetTables()),
            "【目标表中源数据定位】和【字段映射关系】中目标表不一致，请检查配置");

        ResultDO<Long> ret = dataConfigService.addConfig(dataConfig);
        if (ret.getIsSuccess()) {
            return AmisResult.simpleSuccess("success", "保存成功，config id:" + ret.getData());
        } else {
            return AmisResult.fail(ret.getErrorMessage(), null);
        }
    }

    @RequestMapping(value = "/trigger", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult trigger(@RequestParam Long configId, @RequestBody LinkedHashMap<String, Object> paramMap) {
        try {
            String triggerUrl = buildTriggerUrl();
            String runningIp = String.valueOf(paramMap.get("ip"));
            if (StringUtils.isEmpty(runningIp)) {
                runningIp = MachineUtils.getLocalIP();
            }

            ResultDO<DataConfig> queryRet = dataConfigService.getConfig(configId);
            if (!queryRet.getIsSuccess()) {
                return AmisResult.fail("没有找到配置，config id: " + configId, null);
            }

            Long batchId = System.currentTimeMillis();

            DataConfig dataConfig = queryRet.getData();
            DataCheckConfig dataCheckConfig = DataCheckConfig.newInstance(true);

            Map<String, Object> configMap = new HashMap<>();
            configMap.put("dataConfig", dataConfig);
            configMap.put("dataCheckConfig", dataCheckConfig);

            String finalRunningIp = runningIp;
            executorService.submit(() -> {
                ResultDO<DiffResult> ret = dataCheckService.run(batchId, configId, triggerUrl, finalRunningIp, configMap);
            });

            return AmisResult.simpleSuccess("success", "触发成功");
        } catch (Exception e) {
            log.error("failed to trigger compare", e);
            return AmisResult.fail(ErrorMessageUtils.getMessage(e), null);
        }
    }

    @RequestMapping(value = "/stop", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult stop(@RequestParam Long batchId) {
        try {
            String triggerUrl = buildTriggerUrl();

            boolean ret = dataCheckService.stop(batchId);
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

        // 指定batchId查询，最多只有一个
        if (batchIdSearch != null && batchIdSearch > 0) {
            for (DiffResultPO diffResultPO : diffResultPOList) {
                if (batchIdSearch.equals(diffResultPO.getBatchId())) {
                    return diffResultPO;
                }
            }

            return null;
        }

        // 通过时间倒排
        for (DiffResultPO diffResultPO : diffResultPOList) {
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

    private String parseTaskCount(DiffResultPO diffResultPO) {
        StringBuffer sb = new StringBuffer();
        if (diffResultPO != null && diffResultPO.getTotalCount() != null) {
            sb.append(diffResultPO.getTotalCount());
            sb.append(" / ");
        }

        if (diffResultPO != null && diffResultPO.getFailedCount() != null) {
            sb.append(diffResultPO.getFailedCount());
        }

        return sb.toString();
    }

    private boolean filterSearch(String runningIpSearch, String statusSearch, DiffResultPO diffResultPO) {
        if (diffResultPO == null) {
            // 需要单独处理一下not_started，因为这是个默认值，不在diffResultPO中
            return StringUtils.isEmpty(runningIpSearch) &&
                (TaskStatusEnum.NOT_STARTED.getStatus().equalsIgnoreCase(statusSearch) || StringUtils.isEmpty(statusSearch));
        }

        // runningIpSearch
        if (StringUtils.isNotEmpty(runningIpSearch)) {
            if (!runningIpSearch.equals(diffResultPO.getRunningIp())) {
                return false;
            }
        }

        // statusSearch
        if (StringUtils.isNotEmpty(statusSearch)) {
            if (!statusSearch.equals(diffResultPO.getStatus())) {
                return false;
            }
        }

        return true;
    }

    @RequestMapping(value = "/get_config_list", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getConfigList(@RequestParam(required = false) Long configIdSearch, @RequestParam(required = false) Long batchIdSearch,
                                    @RequestParam(required = false) String runningIpSearch, @RequestParam(required = false) String statusSearch) {
        String triggerUrl = buildTriggerUrl();

        ResultDO<Map<Long, List<DiffResultPO>>> queryRet = dataConfigService.getConfigList(configIdSearch);
        if (!queryRet.getIsSuccess()) {
            return AmisResult.emptySuccess();
        }

        List<DataCheckRunningItemVO> dataCheckRunningItemVOList = new ArrayList<>();
        for (Map.Entry<Long, List<DiffResultPO>> entry : queryRet.getData().entrySet()) {
            Long configId = entry.getKey();
            DiffResultPO lastDiffResultPO = getLastDiffResult(entry.getValue(), batchIdSearch);

            if (filterSearch(runningIpSearch, statusSearch, lastDiffResultPO)) {
                DataCheckRunningItemVO dataCheckRunningItemVO = DataCheckRunningItemVO.builder()
                    .batchId(lastDiffResultPO == null ? null : lastDiffResultPO.getBatchId())
                    .configId(configId)
                    .mappingRuleCount(parseMappingRuleCount(lastDiffResultPO))
                    .status(lastDiffResultPO == null ? TaskStatusEnum.NOT_STARTED.getStatus() : lastDiffResultPO.getStatus())
                    .progress(lastDiffResultPO == null ? "0" : lastDiffResultPO.getProgress().substring(0, lastDiffResultPO.getProgress().length() - 1))
                    .runner(lastDiffResultPO == null || StringUtils.isBlank(lastDiffResultPO.getRunner()) ? "" :
                        lastDiffResultPO.getRunner())
                    .taskCount(parseTaskCount(lastDiffResultPO))
                    .runningIp(lastDiffResultPO == null ? null : lastDiffResultPO.getRunningIp())
                    .createdTime(lastDiffResultPO == null ? "" : CommonUtils.getPrettyDate(lastDiffResultPO.getCreatedTime()))
                    .modifiedTime(lastDiffResultPO == null ? "" : CommonUtils.getPrettyDate(lastDiffResultPO.getModifiedTime()))
                    .build();

                dataCheckRunningItemVOList.add(dataCheckRunningItemVO);
            }
        }

        JSONObject retData = new JSONObject();
        retData.put("rows", dataCheckRunningItemVOList);
        retData.put("count", dataCheckRunningItemVOList.size());

        return AmisResult.success(retData, "ok");
    }

    @RequestMapping(value = "/delete_config", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult deleteConfig(@RequestParam Long configId) {
        String triggerUrl = buildTriggerUrl();

        ResultDO<Boolean> ret = dataConfigService.deleteConfig(configId, false);
        if (ret.getIsSuccess()) {
            return AmisResult.simpleSuccess("success", "删除成功，config id:" + configId);
        } else {
            return AmisResult.fail(ret.getErrorMessage(), null);
        }
    }

    @RequestMapping(value = "/get_db_config", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getDbConfig(@RequestParam Long configId) {
        String triggerUrl = buildTriggerUrl();

        DataConfigPO dataConfigPO = dataConfigMapper.getByConfigId(configId);
        if (dataConfigPO == null) {
            log.info("Did not find config for config id: " + configId);
            return AmisResult.emptySuccess();
        }

        DbConfigVO dbConfigVO = DbConfigVO.builder()
            .sourceDriverClassName(dataConfigPO.getSourceDriverClassName())
            .sourceDbUrl(dataConfigPO.getSourceDbUrl())
            .sourceUserName(dataConfigPO.getSourceUserName())
            .sourcePassword(dataConfigPO.getSourcePassword())
            .targetDriverClassName(dataConfigPO.getTargetDriverClassName())
            .targetDbUrl(dataConfigPO.getTargetDbUrl())
            .targetUserName(dataConfigPO.getTargetUserName())
            .targetPassword(dataConfigPO.getTargetPassword())
            .build();

        return AmisResult.success((JSONObject) JSONObject.toJSON(dbConfigVO), "ok");
    }

    @RequestMapping(value = "/update_db_config", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult updateDbConfig(@RequestParam Long configId, @RequestBody DbConfigVO dbConfigVO) {
        String triggerUrl = buildTriggerUrl();

        DataConfigPO dataConfigPO = new DataConfigPO();
        dataConfigPO.setConfigId(configId);
        dataConfigPO.setSourceDriverClassName(dbConfigVO.getSourceDriverClassName());
        dataConfigPO.setSourceDbUrl(dbConfigVO.getSourceDbUrl());
        dataConfigPO.setSourceUserName(dbConfigVO.getSourceUserName());
        dataConfigPO.setSourcePassword(dbConfigVO.getSourcePassword());
        dataConfigPO.setTargetDriverClassName(dbConfigVO.getTargetDriverClassName());
        dataConfigPO.setTargetDbUrl(dbConfigVO.getTargetDbUrl());
        dataConfigPO.setTargetUserName(dbConfigVO.getTargetUserName());
        dataConfigPO.setTargetPassword(dbConfigVO.getTargetPassword());

        int ret = dataConfigMapper.update(dataConfigPO);
        Assert.isTrue(ret == 1, "update db config failed");

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
                .sourceDataSql(sourceInitSqlPO.getInitSql())
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
        sourceInitSqlPO.setInitSql(sourceInitSqlVO.getSourceDataSql());

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
            .runningIp(diffResultPO.getRunningIp())
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

            if (StringUtils.isNotEmpty(sourceSearch) && StringUtils.isNotEmpty(diffDetailPO.getSourceQuery()) &&
                !diffDetailPO.getSourceQuery().contains(sourceSearch)) {
                continue;
            }

            if (StringUtils.isNotEmpty(targetSearch) && StringUtils.isNotEmpty(diffDetailPO.getTargetQuery()) &&
                !diffDetailPO.getTargetQuery().contains(targetSearch)) {
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

    @RequestMapping(value = "/get_ips", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getIps() {
        String triggerUrl = buildTriggerUrl();

        ToolQuery toolQuery = ToolQuery.builder().build();
        ResultDO<List<String>> queryRet = dataConfigService.getSelfIps();
        if (!queryRet.getIsSuccess()) {
            return AmisResult.emptySuccess();
        }

        JSONObject retData = new JSONObject();
        retData.put("options", queryRet.getData());

        return AmisResult.success(retData, "ok");
    }

    private DataConfig convert(DataConfigVO dataConfigVO) {
        DataConfig dataConfig = new DataConfig();
        Long configId = System.currentTimeMillis();
        dataConfig.setConfigId(configId);

        // db链接
        dataConfig.setSourceDbConfig(DatabaseConfig.builder()
            .driverClassName(trim(dataConfigVO.getSourceDriverClassName()))
            .url(trim(dataConfigVO.getSourceDbUrl()))
            .name(trim(dataConfigVO.getSourceUserName()))
            .pwd(trim(dataConfigVO.getSourcePassword()))
            .build());

        dataConfig.setTargetDbConfig(DatabaseConfig.builder()
            .driverClassName(trim(dataConfigVO.getTargetDriverClassName()))
            .url(trim(dataConfigVO.getTargetDbUrl()))
            .name(trim(dataConfigVO.getTargetUserName()))
            .pwd(trim(dataConfigVO.getTargetPassword()))
            .build());

        // mappingRuleList
        List<MappingRule> mappingRuleList = new ArrayList<>();
        JSONArray mappingRuleJA = dataConfigVO.getExcel();
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

        dataConfig.setMappingRuleList(mappingRuleList);

        // tableAndInitSqlMap
        Map<String, String> tableAndInitSqlMap = new HashMap<>();
        Set<String> sourceTableList = dataConfig.getRelatedSourceTables();
        for (String sourceTable : sourceTableList) {
            // 默认补充select * from tablexxx
            tableAndInitSqlMap.put(sourceTable, "select * from " + sourceTable);
        }

        List<SourceInitSqlVO>  sourceInitSqlVOList = dataConfigVO.getInitSqlCombo();
        if (CollectionUtils.isNotEmpty(sourceInitSqlVOList)) {
            for (SourceInitSqlVO sourceInitSqlVO : sourceInitSqlVOList) {
                // 自定义初始化sql
                tableAndInitSqlMap.put(trim(sourceInitSqlVO.getSourceTableName()), trim(sourceInitSqlVO.getSourceDataSql()));
            }
        }
        dataConfig.setTableAndInitSqlMap(tableAndInitSqlMap);

        // tableAndLocatorMethodMap
        Map<String, SourceLocator> tableAndLocatorMap = new HashMap<>();
        List<SourceLocatorVO> sourceLocatorVOList = dataConfigVO.getLocatorCombo();
        for (SourceLocatorVO sourceLocatorVO : sourceLocatorVOList) {
            SourceLocator sourceLocator = SourceLocator.builder()
                .locateField(trim(sourceLocatorVO.getLocateField()))
                .locateMethod(CustomizedMethod.builder()
                    .beanName(trim(sourceLocatorVO.getLocateMethodName()))
                    .args(parseArgFromVO(sourceLocatorVO.getLocateMethodArgs()))
                    .build())
                .build();

            tableAndLocatorMap.put(trim(sourceLocatorVO.getTargetTableName()), sourceLocator);
        }
        dataConfig.setTableAndLocatorMap(tableAndLocatorMap);

        return dataConfig;
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

    public DataCheckConfig parseConfig(String config) {
        DataCheckConfig dataCheckConfig = DataCheckConfig.newInstance(true);

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

        dataCheckConfig.setTableAndCheckFieldsMap(map);
        return dataCheckConfig;
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
