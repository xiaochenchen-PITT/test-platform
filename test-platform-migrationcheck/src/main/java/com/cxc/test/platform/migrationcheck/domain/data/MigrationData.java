package com.cxc.test.platform.migrationcheck.domain.data;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 原表+数据维度
 */
@Data
public class MigrationData {

    private String tableName;

    /**
     * data的全字段数据
     */
    private Map<String, Object> fieldAndValueMap;

    public MigrationData addOneField(String fieldName, Object value) {
        if (fieldAndValueMap == null) {
            fieldAndValueMap = new HashMap<>();
        }

        fieldAndValueMap.put(fieldName, value);
        return this;
    }

    public Object getValue(String fieldName) {
        return fieldAndValueMap.get(fieldName);
    }
}
