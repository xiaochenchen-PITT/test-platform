package com.cxc.test.platform.migrationcheck.domain.config;

import lombok.Setter;

import java.util.*;

public class MigrationCheckConfig {

    // 部分特殊的字段需要提前校验，和提前结束
    private List<String> CHECK_ADVANCE_FIELDS = Arrays.asList("fcooperation_type");

    /**
     * 要对比的字段列表配置
     * 若为空，则全量校验MigrationConfig中的mappingRuleList
     * 若不为空，则只校验这个配置map中的表+字段
     */
    @Setter
    private Map<String, Set<String>> tableAndCheckFieldsMap;

    public Map<String, Set<String>> getTableAndCheckFieldsMap() {
        if (tableAndCheckFieldsMap == null) {
            tableAndCheckFieldsMap = new HashMap<>();
        }

        return tableAndCheckFieldsMap;
    }

    public boolean checkInAdvance(String fieldName) {
        return CHECK_ADVANCE_FIELDS.contains(fieldName);
    }
}
