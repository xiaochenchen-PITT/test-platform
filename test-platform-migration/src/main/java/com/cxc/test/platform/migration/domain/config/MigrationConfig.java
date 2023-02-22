package com.cxc.test.platform.migration.domain.config;

import com.cxc.test.platform.infra.config.DatabaseConfig;
import com.cxc.test.platform.migration.domain.locate.SourceLocator;
import com.cxc.test.platform.migration.domain.mapping.MappingRule;
import com.cxc.test.platform.migration.domain.mapping.SourceMappingItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MigrationConfig {

    /**
     * 全量字段映射关系
     */
    @Getter
    @Setter
    private List<MappingRule> mappingRuleList;

    /**
     * 源数据db的配置
     */
    @Getter
    @Setter
    private DatabaseConfig sourceDbConfig;

    /**
     * 目标数据db的配置
     */
    @Getter
    @Setter
    private DatabaseConfig targetDbConfig;

    /**
     * 源数据的数据初始化sql
     * 可自行定义源数据查询sql。若未自定义，则使用默认的select * from tablexxx 进行全表全字段查询
     * key：源数据表名，value：sql
     */
    @Setter
    private Map<String, String> tableAndSourceInitSqlMap = new HashMap<>();

    /**
     * 目标表中，原表主键id定位方法
     * key：目标数据表名，value：定位方法
     */
    @Getter
    @Setter
    private Map<String, SourceLocator> tableAndLocatorMethodMap = new HashMap<>();

    public Map<String, String> getTableAndSourceInitSqlMap() {
        if (tableAndSourceInitSqlMap == null) {
            tableAndSourceInitSqlMap = new HashMap<>();
        }

        // 已初始化
        if (MapUtils.isNotEmpty(tableAndSourceInitSqlMap)) {
            return tableAndSourceInitSqlMap;
        }

        // 未初始化，使用默认的select * from tablexxx
        for (MappingRule mappingRule : mappingRuleList) {
            String sourceTableName = mappingRule.getSourceMappingItem().getTableName();
            String querySql = String.format("select * from %s", sourceTableName);
            tableAndSourceInitSqlMap.put(sourceTableName, querySql);
        }

        return tableAndSourceInitSqlMap;
    }

    /**
     * 过滤掉只记录主键的mapping rule
     */
    public List<MappingRule> getValidMappingRuleList(String sourceTableName) {
        if (StringUtils.isEmpty(sourceTableName)) {
            return new ArrayList<>();
        }

        return mappingRuleList.stream()
            .filter(mappingRule -> mappingRule.getTargetMappingItem() != null &&
                mappingRule.getSourceMappingItem().getTableName().equals(sourceTableName))
            .collect(Collectors.toList());
    }

    /**
     * 过滤掉只记录主键的mapping rule
     */
    public List<MappingRule> getValidMappingRuleList() {
        return mappingRuleList.stream()
            .filter(mappingRule -> mappingRule.getTargetMappingItem() != null)
            .collect(Collectors.toList());
    }

    public MappingRule getMappingRule(String sourceTableName, String sourceFieldName) {
        if (StringUtils.isEmpty(sourceTableName) || StringUtils.isEmpty(sourceFieldName)) {
            return null;
        }

        for (MappingRule mappingRule : mappingRuleList) {
            SourceMappingItem sourceMappingItem = mappingRule.getSourceMappingItem();
            if (sourceMappingItem.getTableName().equals(sourceTableName) && sourceMappingItem.getFieldName().equals(sourceFieldName)) {
                return mappingRule;
            }
        }

        return null;
    }

    public void addMappingRule(MappingRule mappingRule) {
        if (mappingRuleList == null) {
            mappingRuleList = new ArrayList<>();
        }

        mappingRuleList.add(mappingRule);
    }

}
