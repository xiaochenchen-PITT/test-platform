package com.cxc.test.platform.datacheck.domain.mapping;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Builder
public class SourceMappingItem {

    private final static String FIELD_NAME_SPLITER = ",";

    @Getter
    @Setter
    private String tableName;

    /**
     * 如果是多个字段，则通过,分割
     * 必须属于同一张表
     */
    @Setter
    private String fieldNames;

    @Getter
    private boolean isPrimaryKey = false;

    public List<String> getFieldNameList() {
        if (StringUtils.isEmpty(fieldNames)) {
            return new ArrayList<>();
        }

        if (!fieldNames.contains(FIELD_NAME_SPLITER)) {
            return Arrays.asList(fieldNames);
        }

        List<String> fieldNameList = new ArrayList<>();
        for (String fieldName : fieldNames.split(FIELD_NAME_SPLITER)) {
            if (StringUtils.isNotEmpty(fieldName)) {
                fieldNameList.add(fieldName);
            }
        }

        return fieldNameList;
    }
}
