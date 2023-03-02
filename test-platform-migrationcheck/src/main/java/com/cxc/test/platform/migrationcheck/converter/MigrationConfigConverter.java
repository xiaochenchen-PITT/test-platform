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
import org.springframework.stereotype.Component;

import java.util.*;

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
                        .configId(migrationConfig.getConfigId())
                        .sourceTableName(mappingRule.getSourceMappingItem().getTableName())
                        .sourceFieldName(mappingRule.getSourceMappingItem().getFieldName())
                        .isPrimaryKey(mappingRule.getSourceMappingItem().isPrimaryKey() ? 1 : 0)
                        .targetTableName(mappingRule.getTargetMappingItem().getTableName())
                        .targetFieldName(mappingRule.getTargetMappingItem().getFieldName())
                        .fieldCheckMethodName(mappingRule.getFieldCheckMethod().getBeanName())
                        .fieldCheckMethodArgs(mappingRule.getFieldCheckMethod().getArgs().stream().map(String::valueOf).toString())
                        .build();

                mappingRulePOList.add(mappingRulePO);
            }
        }

        return mappingRulePOList;
    }

    public List<SourceInitSqlPO> convertSourceInitSqlPOList(MigrationConfig migrationConfig) {
        List<SourceInitSqlPO> sourceInitSqlPOList = new ArrayList<>();

        if (MapUtils.isNotEmpty(migrationConfig.getTableAndSourceInitSqlMap())) {
            for (Map.Entry<String, String> entry : migrationConfig.getTableAndSourceInitSqlMap().entrySet()) {
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

        if (MapUtils.isNotEmpty(migrationConfig.getTableAndLocatorMethodMap())) {
            for (Map.Entry<String, SourceLocator> entry : migrationConfig.getTableAndLocatorMethodMap().entrySet()) {
                TargetLocatorPO targetLocatorPO = TargetLocatorPO.builder()
                        .configId(migrationConfig.getConfigId())
                        .targetTableName(entry.getKey())
                        .locateField(entry.getValue().getLocateField())
                        .locateMethodName(entry.getValue().getLocateMethod().getBeanName())
                        .locateMethodArgs(entry.getValue().getLocateMethod().getArgs().stream().map(String::valueOf).toString())
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
        migrationConfig.setTableAndSourceInitSqlMap(tableAndSourceInitSqlMap);

        // tableAndLocatorMethodMap
        Map<String, SourceLocator> tableAndLocatorMethodMap = new HashMap<>();
        for (TargetLocatorPO targetLocatorPO : targetLocatorPOList) {
            SourceLocator sourceLocator = SourceLocator.builder()
                    .locateField(targetLocatorPO.getLocateField())
                    .locateMethod(CustomizedMethod.builder()
                            .beanName(targetLocatorPO.getLocateMethodName())
                            .args(Arrays.asList(targetLocatorPO.getLocateMethodArgs().split(",")))
                            .build())
                    .build();

            tableAndLocatorMethodMap.put(targetLocatorPO.getTargetTableName(), sourceLocator);
        }
        migrationConfig.setTableAndLocatorMethodMap(tableAndLocatorMethodMap);

        return migrationConfig;
    }

    public MappingRule convertPO2DO(MappingRulePO mappingRulePO) {
        MappingRule mappingRule = MappingRule.builder()
                .sourceMappingItem(SourceMappingItem.builder()
                        .tableName(mappingRulePO.getSourceTableName())
                        .fieldName(mappingRulePO.getSourceFieldName())
                        .isPrimaryKey(mappingRulePO.getIsPrimaryKey() == 1)
                        .build())
                .targetMappingItem(TargetMappingItem.builder()
                        .tableName(mappingRulePO.getTargetTableName())
                        .fieldName(mappingRulePO.getTargetFieldName())
                        .build())
                .fieldCheckMethod(CustomizedMethod.builder()
                        .beanName(mappingRulePO.getFieldCheckMethodName())
                        .args(Arrays.asList(mappingRulePO.getFieldCheckMethodArgs().split(",")))
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

}
