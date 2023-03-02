package com.cxc.test.platform.web.migrationcheck.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxc.test.platform.common.domain.AmisResult;
import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.common.utils.ExcelUtils;
import com.cxc.test.platform.infra.config.DatabaseConfig;
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
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@CrossOrigin
@Controller
@RequestMapping(value = "/migrationcheck")
public class MigrationCheckController extends BaseController {

    @Resource
    MigrationService migrationService;

    private final String CONFIG_TABLE_SPLIT = "@";
    private final String CONFIG_FIELD_SPLIT = ",";

    @GetMapping("/config_set")
    public String configSet(){
        return "migrationcheck/configSet";
    }

    @RequestMapping(value = "/add_config", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult addConfig(@RequestBody MigrationConfigVO migrationConfigVO) {
        String triggerUrl = buildTriggerUrl();

        ResultDO<Long> ret = migrationService.addConfig(convert(migrationConfigVO));
        if (ret.getIsSuccess()) {
            return AmisResult.simpleSuccess("success", "保存成功，config id:" + ret.getData());
        } else {
            return AmisResult.fail(ret.getErrorMessage(), null);
        }
    }

    @RequestMapping(value = "/get_config", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getConfig(@RequestParam Long configId) {
        String triggerUrl = buildTriggerUrl();

        ResultDO<MigrationConfig> ret = migrationService.getConfig(configId);


        if (ret.getIsSuccess()) {
            return AmisResult.simpleSuccess("success", "保存成功，config id:" + ret.getData());
        } else {
            return AmisResult.fail(ret.getErrorMessage(), null);
        }
    }


    private MigrationConfig convert(MigrationConfigVO migrationConfigVO) {
        MigrationConfig migrationConfig = new MigrationConfig();
        migrationConfig.setConfigId(System.currentTimeMillis());

        // db链接
        migrationConfig.setSourceDbConfig(DatabaseConfig.builder()
                .driverClassName(migrationConfigVO.getSourceDriverClassName())
                .url(migrationConfigVO.getSourceDbUrl())
                .name(migrationConfigVO.getSourceUserName())
                .pwd(migrationConfigVO.getSourcePassword())
                .build());

        migrationConfig.setTargetDbConfig(DatabaseConfig.builder()
                .driverClassName(migrationConfigVO.getTargetDriverClassName())
                .url(migrationConfigVO.getTargetDbUrl())
                .name(migrationConfigVO.getTargetUserName())
                .pwd(migrationConfigVO.getTargetPassword())
                .build());

        // mappingRuleList
        List<MappingRule> mappingRuleList = new ArrayList<>();
        JSONArray mappingRuleJA = migrationConfigVO.getExcel();
        for (Object mappingRuleO:  mappingRuleJA) {
            JSONObject mappingRuleJO = (JSONObject) mappingRuleO;

            MappingRule mappingRule = MappingRule.builder()
                    .sourceMappingItem(SourceMappingItem.builder()
                            .tableName(mappingRuleJO.getString(MappingRule.SOURCE_TABLE_NAME))
                            .fieldName(mappingRuleJO.getString(MappingRule.SOURCE_FIELD_NAME))
                            .isPrimaryKey(mappingRuleJO.getBoolean(MappingRule.IS_PRIMARY_KEY))
                            .build())
                    .targetMappingItem(TargetMappingItem.builder()
                            .tableName(mappingRuleJO.getString(MappingRule.TARGET_TABLE_NAME))
                            .fieldName(mappingRuleJO.getString(MappingRule.TARGET_FIELD_NAME))
                            .build())
                    .fieldCheckMethod(CustomizedMethod.builder()
                            .beanName(mappingRuleJO.getString(MappingRule.FIELD_CHECK_METHOD_NAME))
                            .args(Arrays.asList(mappingRuleJO.getString(MappingRule.FIELD_CHECK_METHOD_ARGS).split(",")))
                            .build())
                    .build();

            mappingRuleList.add(mappingRule);
        }

        migrationConfig.setMappingRuleList(mappingRuleList);

        // tableAndSourceInitSqlMap
        Map<String, String> tableAndSourceInitSqlMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : migrationConfigVO.getSourceTableSqlKvs().entrySet()) {
            tableAndSourceInitSqlMap.put(entry.getKey(), ((JSONObject)entry.getValue()).getString("sourceDataSql"));
        }
        migrationConfig.setTableAndSourceInitSqlMap(tableAndSourceInitSqlMap);

        // tableAndLocatorMethodMap
        Map<String, SourceLocator> tableAndLocatorMethodMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : migrationConfigVO.getTargetLocatorKvs().entrySet()) {
            JSONObject locatorJO = (JSONObject) entry.getValue();
            SourceLocator sourceLocator = SourceLocator.builder()
                    .locateField(locatorJO.getString("targetLocateField"))
                    .locateMethod(CustomizedMethod.builder()
                            .beanName(locatorJO.getString("targetLocateMethod"))
                            .args(Arrays.asList(locatorJO.getString("targetLocateMethodArgs").split(",")))
                            .build())
                    .build();

            tableAndLocatorMethodMap.put(entry.getKey(), sourceLocator);
        }
        migrationConfig.setTableAndLocatorMethodMap(tableAndLocatorMethodMap);

        return migrationConfig;
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
                        .fieldName(trim(data.get(1)))
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
                log.error(String.format("Failed to parse excel row, data: %s", String.valueOf(data)) , e);
                continue;
            }
        }

        return mappingRuleList;
    }

    private static String trim(String input) {
        if (StringUtils.isEmpty(input)) {
            return null;
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
