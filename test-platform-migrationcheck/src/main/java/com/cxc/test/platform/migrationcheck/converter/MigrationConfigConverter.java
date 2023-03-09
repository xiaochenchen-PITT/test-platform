package com.cxc.test.platform.migrationcheck.converter;

import com.cxc.test.platform.infra.config.DatabaseConfig;
import com.cxc.test.platform.infra.domain.migrationcheck.MappingRulePO;
import com.cxc.test.platform.infra.domain.migrationcheck.MigrationConfigPO;
import com.cxc.test.platform.infra.domain.migrationcheck.SourceInitSqlPO;
import com.cxc.test.platform.infra.domain.migrationcheck.SourceLocatorPO;
import com.cxc.test.platform.migrationcheck.domain.CustomizedMethod;
import com.cxc.test.platform.migrationcheck.domain.SourceLocator;
import com.cxc.test.platform.migrationcheck.domain.config.MigrationConfig;
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
        MigrationConfigPO migrationConfigPO = new MigrationConfigPO();

        migrationConfigPO.setConfigId(migrationConfig.getConfigId());
        migrationConfigPO.setSourceDriverClassName(migrationConfig.getSourceDbConfig().getDriverClassName());
        migrationConfigPO.setSourceDbUrl(migrationConfig.getSourceDbConfig().getUrl());
        migrationConfigPO.setSourceUserName(migrationConfig.getSourceDbConfig().getName());
        migrationConfigPO.setSourcePassword(migrationConfig.getSourceDbConfig().getPwd());
        migrationConfigPO.setTargetDriverClassName(migrationConfig.getTargetDbConfig().getDriverClassName());
        migrationConfigPO.setTargetDbUrl(migrationConfig.getTargetDbConfig().getUrl());
        migrationConfigPO.setTargetUserName(migrationConfig.getTargetDbConfig().getName());
        migrationConfigPO.setTargetPassword(migrationConfig.getTargetDbConfig().getPwd());

        return migrationConfigPO;
    }

    public List<MappingRulePO> convertMappingRulePOList(MigrationConfig migrationConfig) {
        List<MappingRulePO> mappingRulePOList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(migrationConfig.getMappingRuleList())) {
            for (MappingRule mappingRule : migrationConfig.getMappingRuleList()) {
                MappingRulePO mappingRulePO = new MappingRulePO();
                mappingRulePO.setConfigId(migrationConfig.getConfigId());
                mappingRulePO.setSourceTableName(mappingRule.getSourceMappingItem().getTableName());
                mappingRulePO.setSourceFieldNames(String.join(",", mappingRule.getSourceMappingItem().getFieldNameList()));
                mappingRulePO.setIsPrimaryKey(mappingRule.getSourceMappingItem().isPrimaryKey() ? 1 : 0);
                mappingRulePO.setTargetTableName(mappingRule.getTargetMappingItem().getTableName());
                mappingRulePO.setTargetFieldName(mappingRule.getTargetMappingItem().getFieldName());
                mappingRulePO.setFieldCheckMethodName(mappingRule.getFieldCheckMethod().getBeanName());
                mappingRulePO.setFieldCheckMethodArgs(String.valueOf(mappingRule.getFieldCheckMethod().getArgs()));

                mappingRulePOList.add(mappingRulePO);
            }
        }

        return mappingRulePOList;
    }

    public List<SourceInitSqlPO> convertSourceInitSqlPOList(MigrationConfig migrationConfig) {
        List<SourceInitSqlPO> sourceInitSqlPOList = new ArrayList<>();

        if (MapUtils.isNotEmpty(migrationConfig.getTableAndInitSqlMap())) {
            for (Map.Entry<String, String> entry : migrationConfig.getTableAndInitSqlMap().entrySet()) {
                SourceInitSqlPO sourceInitSqlPO = new SourceInitSqlPO();
                sourceInitSqlPO.setConfigId(migrationConfig.getConfigId());
                sourceInitSqlPO.setSourceTableName(entry.getKey());
                sourceInitSqlPO.setInitSql(entry.getValue());

                sourceInitSqlPOList.add(sourceInitSqlPO);
            }
        }

        return sourceInitSqlPOList;
    }

    public List<SourceLocatorPO> convertLocatorPOList(MigrationConfig migrationConfig) {
        List<SourceLocatorPO> sourceLocatorPOList = new ArrayList<>();

        if (MapUtils.isNotEmpty(migrationConfig.getTableAndLocatorMap())) {
            for (Map.Entry<String, SourceLocator> entry : migrationConfig.getTableAndLocatorMap().entrySet()) {
                SourceLocatorPO sourceLocatorPO = new SourceLocatorPO();
                sourceLocatorPO.setConfigId(migrationConfig.getConfigId());
                sourceLocatorPO.setTargetTableName(entry.getKey());
                sourceLocatorPO.setLocateField(entry.getValue().getLocateField());
                sourceLocatorPO.setLocateMethodName(entry.getValue().getLocateMethod().getBeanName());
                sourceLocatorPO.setLocateMethodArgs(String.valueOf(entry.getValue().getLocateMethod().getArgs()));

                sourceLocatorPOList.add(sourceLocatorPO);
            }
        }

        return sourceLocatorPOList;
    }

    public MigrationConfig convertPO2DO(MigrationConfigPO migrationConfigPO, List<MappingRulePO> mappingRulePOList,
                                        List<SourceLocatorPO> sourceLocatorPOList, List<SourceInitSqlPO> sourceInitSqlPOList) {
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

        // tableAndLocatorMap
        Map<String, SourceLocator> tableAndLocatorMap = new HashMap<>();
        for (SourceLocatorPO sourceLocatorPO : sourceLocatorPOList) {
            SourceLocator sourceLocator = SourceLocator.builder()
                .locateField(sourceLocatorPO.getLocateField())
                .locateMethod(CustomizedMethod.builder()
                    .beanName(sourceLocatorPO.getLocateMethodName())
                    .args(convertArg(sourceLocatorPO.getLocateMethodArgs()))
                    .build())
                .build();

            tableAndLocatorMap.put(sourceLocatorPO.getTargetTableName(), sourceLocator);
        }
        migrationConfig.setTableAndLocatorMap(tableAndLocatorMap);

        return migrationConfig;
    }

    public MappingRule convertPO2DO(MappingRulePO mappingRulePO) {
        MappingRule mappingRule = MappingRule.builder()
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
