package com.cxc.test.platform.migrationcheck.domain.config;

import com.cxc.test.platform.infra.config.DatabaseConfig;
import com.cxc.test.platform.migrationcheck.domain.locate.SourceLocator;
import com.cxc.test.platform.migrationcheck.domain.mapping.MappingRule;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
@Data
public class MigrationConfig {

    private Long configId;

    /**
     * 全量字段映射关系
     */
    private List<MappingRule> mappingRuleList;

    /**
     * 源数据db的配置
     */
    private DatabaseConfig sourceDbConfig;

    /**
     * 目标数据db的配置
     */
    private DatabaseConfig targetDbConfig;

    /**
     * 源数据的数据初始化sql
     * key：源数据表名，value：sql
     */
    private Map<String, String> tableAndInitSqlMap = new HashMap<>();

    /**
     * 目标表中，原表主键id定位方法
     * key：目标表名，value：定位方法
     */
    private Map<String, SourceLocator> tableFieldAndLocatorMap = new HashMap<>();

    public Set<String> getRelatedSourceTables() {
        return mappingRuleList.stream()
                .filter(MappingRule::hasSourceTable)
                .map(mappingRule -> mappingRule.getSourceMappingItem().getTableName())
                .collect(Collectors.toSet());
    }

    public Set<String> getRelatedTargetTables() {
        return mappingRuleList.stream()
                .filter(MappingRule::hasTargetTable)
                .map(mappingRule -> mappingRule.getTargetMappingItem().getTableName())
                .collect(Collectors.toSet());
    }

    public List<MappingRule> getValidMappingRuleList(String sourceTableName, boolean isCovered) {
        if (StringUtils.isEmpty(sourceTableName)) {
            return new ArrayList<>();
        }

        List<MappingRule> validList = mappingRuleList.stream()
                .filter(mappingRule -> {
                    // 过滤掉只记录主键的mapping rule，没有target表的信息
                    if (!mappingRule.hasTargetTable()) {
                        return false;
                    }

                    // 还有一种情况是target表中的值和source表无关，需要兼容（例如为写死字段，或者有其他和source表无关的校验方式）
                    if (!isCovered && !mappingRule.hasSourceTable()) {
                        return true;
                    }

                    return StringUtils.equals(mappingRule.getSourceMappingItem().getTableName(), sourceTableName);
                })
                .collect(Collectors.toList());

        return validList;
    }

    public List<MappingRule> getValidMappingRuleList() {
        return mappingRuleList.stream()
                .filter(mappingRule -> {
                    // 过滤掉只记录主键的mapping rule，没有target表的信息
                    if (!mappingRule.hasTargetTable()) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    public void addMappingRule(MappingRule mappingRule) {
        if (mappingRuleList == null) {
            mappingRuleList = new ArrayList<>();
        }

        mappingRuleList.add(mappingRule);
    }

}
