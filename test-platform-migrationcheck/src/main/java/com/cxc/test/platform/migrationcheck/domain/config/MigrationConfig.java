package com.cxc.test.platform.migrationcheck.domain.config;

import com.cxc.test.platform.infra.config.DatabaseConfig;
import com.cxc.test.platform.migrationcheck.domain.locate.SourceLocator;
import com.cxc.test.platform.migrationcheck.domain.mapping.MappingRule;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;
@Data
public class MigrationConfig {

    public static final String TABLE_AND_FIELD_JOINER = "#";

    private Long configId;

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
     * key：目标表名#目标字段名，value：定位方法
     * #使用TABLE_AND_FIELD_JOINER
     */
    @Getter
    @Setter
    private Map<String, SourceLocator> tableFieldAndLocatorMap = new HashMap<>();

    public Set<String> getRelatedSourceTables() {
        return mappingRuleList.stream()
                .filter(MappingRule::hasSourceTable)
                .map(mappingRule -> mappingRule.getSourceMappingItem().getTableName())
                .collect(Collectors.toSet());
    }

    public Map<String, String> getTableAndSourceInitSqlMap() {
        if (tableAndSourceInitSqlMap == null) {
            tableAndSourceInitSqlMap = new HashMap<>();
        }

        // 已初始化
        if (MapUtils.isNotEmpty(tableAndSourceInitSqlMap)) {
            Assert.isTrue(tableAndSourceInitSqlMap.size() == getRelatedSourceTables().size(),
                    "some of the source tables do not have init sql, please check.");
            return tableAndSourceInitSqlMap;
        }

        // 未初始化，使用默认的select * from tablexxx
        for (MappingRule mappingRule : mappingRuleList) {
            if (mappingRule.hasSourceTable()) {
                String sourceTableName = mappingRule.getSourceMappingItem().getTableName();
                String querySql = String.format("select * from %s", sourceTableName);
                tableAndSourceInitSqlMap.put(sourceTableName, querySql);
            }
        }

        return tableAndSourceInitSqlMap;
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
