package com.cxc.test.platform.migration.controller;

import com.cxc.test.platform.common.utils.ExcelUtils;
import com.cxc.test.platform.migration.domain.CustomizedMethod;
import com.cxc.test.platform.migration.domain.config.MigrationCheckConfig;
import com.cxc.test.platform.migration.domain.mapping.MappingRule;
import com.cxc.test.platform.migration.domain.mapping.SourceMappingItem;
import com.cxc.test.platform.migration.domain.mapping.TargetMappingItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Slf4j
@CrossOrigin
@RestController
public class MigrationCheckBaseController {

    @Resource
    HttpServletRequest request;

    private final String CONFIG_TABLE_SPLIT = "@";
    private final String CONFIG_FIELD_SPLIT = ",";

    public MigrationCheckConfig parseConfig(String config) {
        MigrationCheckConfig migrationCheckConfig = new MigrationCheckConfig();

        // tableAndCheckFieldsMap
        Map<String, Set<String>> map = new HashMap<>();
        if (StringUtils.isEmpty(config)) {
            migrationCheckConfig.setTableAndCheckFieldsMap(map);
        } else {
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

            migrationCheckConfig.setTableAndCheckFieldsMap(map);
        }

        return migrationCheckConfig;
    }

    public String buildTriggerUrl() {
        StringBuffer triggerUrlSb = request.getRequestURL();
        triggerUrlSb.append("?");
        boolean first = true;
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if (first) {
                triggerUrlSb.append("?");
            } else {
                triggerUrlSb.append("&");
            }

            triggerUrlSb.append(entry.getKey()).append("=").append(Arrays.toString(entry.getValue()));
            first = false;
        }

        String triggerUrl = triggerUrlSb.toString();
        log.info("trigger url: {}", triggerUrl);
        return triggerUrl;
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
