package com.cxc.test.platform.datacheck.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 一张表的一行记录对应一个SourceData
 */
@Data
public class SourceData {

    private String tableName;

    /**
     * data的全字段数据
     */
    private Map<String, Object> fieldAndValueMap;

    public SourceData addOneField(String fieldName, Object value) {
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
