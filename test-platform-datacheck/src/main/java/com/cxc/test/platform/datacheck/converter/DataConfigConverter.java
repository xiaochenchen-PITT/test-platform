package com.cxc.test.platform.datacheck.converter;

import com.cxc.test.platform.infra.config.DatabaseConfig;
import com.cxc.test.platform.infra.domain.datacheck.MappingRulePO;
import com.cxc.test.platform.infra.domain.datacheck.DataConfigPO;
import com.cxc.test.platform.infra.domain.datacheck.SourceInitSqlPO;
import com.cxc.test.platform.infra.domain.datacheck.SourceLocatorPO;
import com.cxc.test.platform.datacheck.domain.CustomizedMethod;
import com.cxc.test.platform.datacheck.domain.SourceLocator;
import com.cxc.test.platform.datacheck.domain.config.DataConfig;
import com.cxc.test.platform.datacheck.domain.mapping.MappingRule;
import com.cxc.test.platform.datacheck.domain.mapping.SourceMappingItem;
import com.cxc.test.platform.datacheck.domain.mapping.TargetMappingItem;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataConfigConverter {

    public DataConfigPO convertConfigPO(DataConfig dataConfig) {
        DataConfigPO dataConfigPO = new DataConfigPO();

        dataConfigPO.setConfigId(dataConfig.getConfigId());
        dataConfigPO.setSourceDriverClassName(dataConfig.getSourceDbConfig().getDriverClassName());
        dataConfigPO.setSourceDbUrl(dataConfig.getSourceDbConfig().getUrl());
        dataConfigPO.setSourceUserName(dataConfig.getSourceDbConfig().getName());
        dataConfigPO.setSourcePassword(dataConfig.getSourceDbConfig().getPwd());
        dataConfigPO.setTargetDriverClassName(dataConfig.getTargetDbConfig().getDriverClassName());
        dataConfigPO.setTargetDbUrl(dataConfig.getTargetDbConfig().getUrl());
        dataConfigPO.setTargetUserName(dataConfig.getTargetDbConfig().getName());
        dataConfigPO.setTargetPassword(dataConfig.getTargetDbConfig().getPwd());

        return dataConfigPO;
    }

    public List<MappingRulePO> convertMappingRulePOList(DataConfig dataConfig) {
        List<MappingRulePO> mappingRulePOList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(dataConfig.getMappingRuleList())) {
            for (MappingRule mappingRule : dataConfig.getMappingRuleList()) {
                MappingRulePO mappingRulePO = new MappingRulePO();
                mappingRulePO.setConfigId(dataConfig.getConfigId());
                mappingRulePO.setSourceTableName(mappingRule.getSourceMappingItem().getTableName());
                mappingRulePO.setSourceFieldNames(String.join(",", mappingRule.getSourceMappingItem().getFieldNameList()));
                mappingRulePO.setIsPrimaryKey(mappingRule.getSourceMappingItem().isPrimaryKey() ? 1 : 0);
                mappingRulePO.setTargetTableName(mappingRule.getTargetMappingItem().getTableName());
                mappingRulePO.setTargetFieldName(mappingRule.getTargetMappingItem().getFieldName());
                mappingRulePO.setFieldCheckMethodName(mappingRule.getFieldCheckMethod().getBeanName());
                mappingRulePO.setFieldCheckMethodArgs(CollectionUtils.isNotEmpty(mappingRule.getFieldCheckMethod().getArgs()) ?
                    String.valueOf(mappingRule.getFieldCheckMethod().getArgs()) : "");

                mappingRulePOList.add(mappingRulePO);
            }
        }

        return mappingRulePOList;
    }

    public List<SourceInitSqlPO> convertSourceInitSqlPOList(DataConfig dataConfig) {
        List<SourceInitSqlPO> sourceInitSqlPOList = new ArrayList<>();

        if (MapUtils.isNotEmpty(dataConfig.getTableAndInitSqlMap())) {
            for (Map.Entry<String, String> entry : dataConfig.getTableAndInitSqlMap().entrySet()) {
                SourceInitSqlPO sourceInitSqlPO = new SourceInitSqlPO();
                sourceInitSqlPO.setConfigId(dataConfig.getConfigId());
                sourceInitSqlPO.setSourceTableName(entry.getKey());
                sourceInitSqlPO.setInitSql(entry.getValue());

                sourceInitSqlPOList.add(sourceInitSqlPO);
            }
        }

        return sourceInitSqlPOList;
    }

    public List<SourceLocatorPO> convertLocatorPOList(DataConfig dataConfig) {
        List<SourceLocatorPO> sourceLocatorPOList = new ArrayList<>();

        if (MapUtils.isNotEmpty(dataConfig.getTableAndLocatorMap())) {
            for (Map.Entry<String, SourceLocator> entry : dataConfig.getTableAndLocatorMap().entrySet()) {
                SourceLocatorPO sourceLocatorPO = new SourceLocatorPO();
                sourceLocatorPO.setConfigId(dataConfig.getConfigId());
                sourceLocatorPO.setTargetTableName(entry.getKey());
                sourceLocatorPO.setLocateField(entry.getValue().getLocateField());
                sourceLocatorPO.setLocateMethodName(entry.getValue().getLocateMethod().getBeanName());
                sourceLocatorPO.setLocateMethodArgs(CollectionUtils.isNotEmpty(entry.getValue().getLocateMethod().getArgs()) ?
                    String.valueOf(entry.getValue().getLocateMethod().getArgs()) : null);

                sourceLocatorPOList.add(sourceLocatorPO);
            }
        }

        return sourceLocatorPOList;
    }

    public DataConfig convertPO2DO(DataConfigPO dataConfigPO, List<MappingRulePO> mappingRulePOList,
                                   List<SourceLocatorPO> sourceLocatorPOList, List<SourceInitSqlPO> sourceInitSqlPOList) {
        DataConfig dataConfig = new DataConfig();
        dataConfig.setConfigId(dataConfigPO.getConfigId());

        // db链接
        dataConfig.setSourceDbConfig(DatabaseConfig.builder()
            .driverClassName(dataConfigPO.getSourceDriverClassName())
            .url(dataConfigPO.getSourceDbUrl())
            .name(dataConfigPO.getSourceUserName())
            .pwd(dataConfigPO.getSourcePassword())
            .build());

        dataConfig.setTargetDbConfig(DatabaseConfig.builder()
            .driverClassName(dataConfigPO.getTargetDriverClassName())
            .url(dataConfigPO.getTargetDbUrl())
            .name(dataConfigPO.getTargetUserName())
            .pwd(dataConfigPO.getTargetPassword())
            .build());

        // 全量字段映射关系
        dataConfig.setMappingRuleList(convertPO2DO(mappingRulePOList));

        // tableAndSourceInitSqlMap
        Map<String, String> tableAndSourceInitSqlMap = new HashMap<>();
        for (SourceInitSqlPO sourceInitSqlPO : sourceInitSqlPOList) {
            tableAndSourceInitSqlMap.put(sourceInitSqlPO.getSourceTableName(), sourceInitSqlPO.getInitSql());
        }
        dataConfig.setTableAndInitSqlMap(tableAndSourceInitSqlMap);

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
        dataConfig.setTableAndLocatorMap(tableAndLocatorMap);

        return dataConfig;
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
