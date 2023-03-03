package com.cxc.test.platform.migrationcheck.converter;

import com.cxc.test.platform.infra.config.DatabaseConfig;
import com.cxc.test.platform.infra.domain.migrationcheck.MappingRulePO;
import com.cxc.test.platform.infra.domain.migrationcheck.MigrationConfigPO;
import com.cxc.test.platform.infra.domain.migrationcheck.SourceInitSqlPO;
import com.cxc.test.platform.infra.domain.migrationcheck.TargetLocatorPO;
import com.cxc.test.platform.migrationcheck.domain.CustomizedMethod;
import com.cxc.test.platform.migrationcheck.domain.config.MigrationConfig;
import com.cxc.test.platform.migrationcheck.domain.locate.SourceLocator;
import com.cxc.test.platform.migrationcheck.domain.mapping.MappingRule;
import com.cxc.test.platform.migrationcheck.domain.mapping.SourceMappingItem;
import com.cxc.test.platform.migrationcheck.domain.mapping.TargetMappingItem;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MigrationConfigConverter {

    public MigrationConfigPO convertMigrationConfigPO(MigrationConfig migrationConfig) {
        MigrationConfigPO migrationConfigPO = MigrationConfigPO.builder()
                .configId(migrationConfig.getConfigId())
                .sourceDriverClassName(migrationConfig.getSourceDbConfig().getDriverClassName())
                .sourceDbUrl(migrationConfig.getSourceDbConfig().getUrl())
                .sourceUserName(migrationConfig.getSourceDbConfig().getName())
                .sourcePassword(migrationConfig.getSourceDbConfig().getPwd())
                .targetDriverClassName(migrationConfig.getTargetDbConfig().getDriverClassName())
                .targetDbUrl(migrationConfig.getTargetDbConfig().getUrl())
                .targetUserName(migrationConfig.getTargetDbConfig().getName())
                .targetPassword(migrationConfig.getTargetDbConfig().getPwd())
                .build();

        return migrationConfigPO;
    }

    public List<MappingRulePO> convertMappingRulePOList(MigrationConfig migrationConfig) {
        List<MappingRulePO> mappingRulePOList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(migrationConfig.getMappingRuleList())) {
            for (MappingRule mappingRule : migrationConfig.getMappingRuleList()) {
                MappingRulePO mappingRulePO = MappingRulePO.builder()
                        .id(mappingRule.getId())
                        .configId(migrationConfig.getConfigId())
                        .sourceTableName(mappingRule.getSourceMappingItem().getTableName())
                        .sourceFieldNames(String.join(",", mappingRule.getSourceMappingItem().getFieldNameList()))
                        .isPrimaryKey(mappingRule.getSourceMappingItem().isPrimaryKey() ? 1 : 0)
                        .targetTableName(mappingRule.getTargetMappingItem().getTableName())
                        .targetFieldName(mappingRule.getTargetMappingItem().getFieldName())
                        .fieldCheckMethodName(mappingRule.getFieldCheckMethod().getBeanName())
                        .fieldCheckMethodArgs(String.valueOf(mappingRule.getFieldCheckMethod().getArgs()))
                        .build();

                mappingRulePOList.add(mappingRulePO);
            }
        }

        return mappingRulePOList;
    }

    public List<SourceInitSqlPO> convertSourceInitSqlPOList(MigrationConfig migrationConfig) {
        List<SourceInitSqlPO> sourceInitSqlPOList = new ArrayList<>();

        if (MapUtils.isNotEmpty(migrationConfig.getTableAndInitSqlMap())) {
            for (Map.Entry<String, String> entry : migrationConfig.getTableAndInitSqlMap().entrySet()) {
                SourceInitSqlPO sourceInitSqlPO = SourceInitSqlPO.builder()
                        .configId(migrationConfig.getConfigId())
                        .sourceTableName(entry.getKey())
                        .initSql(entry.getValue())
                        .build();

                sourceInitSqlPOList.add(sourceInitSqlPO);
            }
        }

        return sourceInitSqlPOList;
    }

    public List<TargetLocatorPO> convertTargetLocatorPOList(MigrationConfig migrationConfig) {
        List<TargetLocatorPO> targetLocatorPOList = new ArrayList<>();

        if (MapUtils.isNotEmpty(migrationConfig.getTableFieldAndLocatorMap())) {
            for (Map.Entry<String, SourceLocator> entry : migrationConfig.getTableFieldAndLocatorMap().entrySet()) {
                TargetLocatorPO targetLocatorPO = TargetLocatorPO.builder()
                        .configId(migrationConfig.getConfigId())
                        .targetTableName(entry.getKey())
                        .locateField(entry.getValue().getLocateField())
                        .locateMethodName(entry.getValue().getLocateMethod().getBeanName())
                        .locateMethodArgs(String.valueOf(entry.getValue().getLocateMethod().getArgs()))
                        .build();

                targetLocatorPOList.add(targetLocatorPO);
            }
        }

        return targetLocatorPOList;
    }

    public MigrationConfig convertPO2DO(MigrationConfigPO migrationConfigPO, List<MappingRulePO> mappingRulePOList,
                                        List<TargetLocatorPO> targetLocatorPOList, List<SourceInitSqlPO> sourceInitSqlPOList) {
        MigrationConfig migrationConfig = new MigrationConfig();
        migrationConfig.setConfigId(migrationConfigPO.getConfigId());

        // db链接
        migrationConfig.setSourceDbConfig(DatabaseConfig.builder()
                .driverClassName(migrationConfigPO.getSourceDriverClassName())
                .url(migrationConfigPO.getSourceDbUrl())
                .name(migrationConfigPO.getSourceUserName())
                .pwd(migrationConfigPO.getSourcePassword())
                .build());

        migrationConfig.setTargetDbConfig(DatabaseConfig.builder()
                .driverClassName(migrationConfigPO.getTargetDriverClassName())
                .url(migrationConfigPO.getTargetDbUrl())
                .name(migrationConfigPO.getTargetUserName())
                .pwd(migrationConfigPO.getTargetPassword())
                .build());

        // 全量字段映射关系
        migrationConfig.setMappingRuleList(convertPO2DO(mappingRulePOList));

        // tableAndSourceInitSqlMap
        Map<String, String> tableAndSourceInitSqlMap = new HashMap<>();
        for (SourceInitSqlPO sourceInitSqlPO : sourceInitSqlPOList) {
            tableAndSourceInitSqlMap.put(sourceInitSqlPO.getSourceTableName(), sourceInitSqlPO.getInitSql());
        }
        migrationConfig.setTableAndInitSqlMap(tableAndSourceInitSqlMap);

        // tableFieldAndLocatorMap
        Map<String, SourceLocator> tableFieldAndLocatorMap = new HashMap<>();
        for (TargetLocatorPO targetLocatorPO : targetLocatorPOList) {
            SourceLocator sourceLocator = SourceLocator.builder()
                    .locateField(targetLocatorPO.getLocateField())
                    .locateMethod(CustomizedMethod.builder()
                            .beanName(targetLocatorPO.getLocateMethodName())
                            .args(convertArg(targetLocatorPO.getLocateMethodArgs()))
                            .build())
                    .build();

            tableFieldAndLocatorMap.put(targetLocatorPO.getTargetTableName(), sourceLocator);
        }
        migrationConfig.setTableFieldAndLocatorMap(tableFieldAndLocatorMap);

        return migrationConfig;
    }

    public MappingRule convertPO2DO(MappingRulePO mappingRulePO) {
        MappingRule mappingRule = MappingRule.builder()
                .id(mappingRulePO.getId())
                .sourceMappingItem(SourceMappingItem.builder()
                        .tableName(mappingRulePO.getSourceTableName())
                        .fieldNames(mappingRulePO.getSourceFieldNames())
                        .isPrimaryKey(mappingRulePO.getIsPrimaryKey() == 1)
                        .build())
                .targetMappingItem(TargetMappingItem.builder()
                        .tableName(mappingRulePO.getTargetTableName())
                        .fieldName(mappingRulePO.getTargetFieldName())
                        .build())
                .fieldCheckMethod(CustomizedMethod.builder()
                        .beanName(mappingRulePO.getFieldCheckMethodName())
                        .args(convertArg(mappingRulePO.getFieldCheckMethodArgs()))
                        .build())
                .build();

        return mappingRule;
    }

    public List<MappingRule> convertPO2DO(List<MappingRulePO> mappingRulePOList) {
        List<MappingRule> mappingRuleList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(mappingRulePOList)) {
            for (MappingRulePO mappingRulePO : mappingRulePOList) {
                mappingRuleList.add(convertPO2DO(mappingRulePO));
            }
        }

        return mappingRuleList;
    }

    private List<Object> convertArg(String argsStr) {
        if (StringUtils.isEmpty(argsStr)) {
            return null;
        }

        if (argsStr.startsWith("[")) {
            argsStr = argsStr.substring(1);
        }

        if (argsStr.endsWith("]")) {
            argsStr = argsStr.substring(0, argsStr.length() - 1);
        }

        return Arrays.stream(argsStr.split(","))
                .filter(StringUtils::isNotEmpty)
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
