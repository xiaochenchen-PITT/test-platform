package com.cxc.test.platform.web.migrationcheck.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxc.test.platform.common.domain.AmisResult;
import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.common.utils.ExcelUtils;
import com.cxc.test.platform.infra.config.DatabaseConfig;
import com.cxc.test.platform.infra.domain.migrationcheck.MappingRulePO;
import com.cxc.test.platform.infra.mapper.xytest.MappingRuleMapper;
import com.cxc.test.platform.migrationcheck.domain.CustomizedMethod;
import com.cxc.test.platform.migrationcheck.domain.config.MigrationCheckConfig;
import com.cxc.test.platform.migrationcheck.domain.config.MigrationConfig;
import com.cxc.test.platform.migrationcheck.domain.locate.SourceLocator;
import com.cxc.test.platform.migrationcheck.domain.mapping.MappingRule;
import com.cxc.test.platform.migrationcheck.domain.mapping.SourceMappingItem;
import com.cxc.test.platform.migrationcheck.domain.mapping.TargetMappingItem;
import com.cxc.test.platform.migrationcheck.service.MigrationService;
import com.cxc.test.platform.web.BaseController;
import com.cxc.test.platform.web.migrationcheck.vo.MigrationConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin
@Controller
@RequestMapping(value = "/migrationcheck")
public class MigrationCheckController extends BaseController {

    @Resource
    MigrationService migrationService;

    @Resource
    MappingRuleMapper mappingRuleMapper;

    private final String CONFIG_TABLE_SPLIT = "@";
    private final String CONFIG_FIELD_SPLIT = ",";

    @GetMapping("/config_set")
    public String configSet() {
        return "migrationcheck/configSet";
    }

    @GetMapping("/config_detail")
    public String configDetail() {
        return "migrationcheck/configDetail";
    }

    @RequestMapping(value = "/add_config", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult addConfig(@RequestBody MigrationConfigVO migrationConfigVO) {
        String triggerUrl = buildTriggerUrl();

        MigrationConfig migrationConfig = convert(migrationConfigVO);

        Assert.isTrue(migrationConfig.getTableAndInitSqlMap().size() == migrationConfig.getRelatedSourceTables().size(),
                "【源数据初始化】和【字段映射关系】中源表数量不一致，请检查配置");
        Assert.isTrue(migrationConfig.getTableFieldAndLocatorMap().size() == migrationConfig.getRelatedTargetTables().size(),
                "【目标表中源数据定位】和【字段映射关系】中目标表数量不一致，请检查配置");

        ResultDO<Long> ret = migrationService.addConfig(migrationConfig);
        if (ret.getIsSuccess()) {
            return AmisResult.simpleSuccess("success", "保存成功，config id:" + ret.getData());
        } else {
            return AmisResult.fail(ret.getErrorMessage(), null);
        }
    }

    @RequestMapping(value = "/get_mapping_rule_config", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getMappingRuleConfig(@RequestParam Long configId, @RequestParam(required = false) String sourceTableNameSearch,
                                           @RequestParam(required = false) String targetTableNameSearch) {
        String triggerUrl = buildTriggerUrl();

        ResultDO<MigrationConfig> ret = migrationService.getConfig(configId);
        if (!ret.getIsSuccess()) {
            return AmisResult.fail(ret.getErrorMessage(), null);
        }

        List<MappingRule> mappingRuleList = ret.getData().getMappingRuleList();

        JSONObject retData = new JSONObject();
        JSONArray rowJA = new JSONArray();
        for (MappingRule mappingRule : mappingRuleList) {
            if (StringUtils.isNotEmpty(sourceTableNameSearch) && StringUtils.isNotEmpty(mappingRule.getSourceMappingItem().getTableName())
                    && !mappingRule.getSourceMappingItem().getTableName().contains(sourceTableNameSearch)) {
                continue;
            }

            if (StringUtils.isNotEmpty(targetTableNameSearch) && StringUtils.isNotEmpty(mappingRule.getTargetMappingItem().getTableName())
                    && !mappingRule.getTargetMappingItem().getTableName().contains(targetTableNameSearch)) {
                continue;
            }

            JSONObject rowJO = new JSONObject();
            rowJO.put("id", mappingRule.getId());
            rowJO.put("sourceTableName", mappingRule.getSourceMappingItem().getTableName());
            rowJO.put("sourceFieldNames", mappingRule.getSourceMappingItem().getFieldNameList());
            rowJO.put("isPrimaryKey", mappingRule.getSourceMappingItem().isPrimaryKey());
            rowJO.put("targetTableName", mappingRule.getTargetMappingItem().getTableName());
            rowJO.put("targetFieldName", mappingRule.getTargetMappingItem().getFieldName());
            rowJO.put("fieldCheckMethodName", mappingRule.getFieldCheckMethod().getBeanName());
            rowJO.put("fieldCheckMethodArgs", mappingRule.getFieldCheckMethod().getArgs());

            rowJA.add(rowJO);
        }
        retData.put("rows", rowJA);
        retData.put("count", rowJA.size());

        return AmisResult.success(retData, "ok");
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

    @RequestMapping(value = "/update_mapping_rule", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult updateMappingRuleConfig(@RequestParam Long id, @RequestParam String sourceTableName,
                                              @RequestParam String sourceFieldNames, @RequestParam String isPrimaryKey,
                                              @RequestParam String targetTableName, @RequestParam String targetFieldName,
                                              @RequestParam String fieldCheckMethodName, @RequestParam String fieldCheckMethodArgs) {
        String triggerUrl = buildTriggerUrl();

        MappingRulePO mappingRulePO = MappingRulePO.builder()
                .id(id)
                .sourceTableName(sourceTableName)
                .sourceFieldNames(sourceFieldNames)
                .isPrimaryKey(isPrimaryKey.equalsIgnoreCase("true") ? 1 : 0)
                .targetTableName(targetTableName)
                .targetFieldName(targetFieldName)
                .fieldCheckMethodName(fieldCheckMethodName)
                .fieldCheckMethodArgs(fieldCheckMethodArgs)
                .build();

        int ret = mappingRuleMapper.update(mappingRulePO);
        Assert.isTrue(ret == 1, "update mapping rule failed");

        return AmisResult.simpleSuccess("success", "编辑成功");
    }

    private MigrationConfig convert(MigrationConfigVO migrationConfigVO) {
        MigrationConfig migrationConfig = new MigrationConfig();
        migrationConfig.setConfigId(System.currentTimeMillis());

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
        Map<String, SourceLocator> tableFieldAndLocatorMap = new HashMap<>();
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

            tableFieldAndLocatorMap.put(trim(locatorComboJO.getString("targetTableName")), sourceLocator);
        }
        migrationConfig.setTableFieldAndLocatorMap(tableFieldAndLocatorMap);

        return migrationConfig;
    }

    private List<Object> parseArgFromVO(String argsStr) {
        if (StringUtils.isBlank(argsStr)) {
            return null;
        }

        return Arrays.stream(argsStr.split(","))
                .map(s -> trim(s))
                .collect(Collectors.toList());
    }

    /**********************************************************************************
     * old
     *********************************************************************************/

    public MigrationCheckConfig parseConfig(String config) {
        MigrationCheckConfig migrationCheckConfig = new MigrationCheckConfig();

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
