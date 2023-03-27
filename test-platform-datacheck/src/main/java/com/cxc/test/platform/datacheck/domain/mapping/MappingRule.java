package com.cxc.test.platform.datacheck.domain.mapping;

import com.cxc.test.platform.datacheck.domain.CustomizedMethod;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Builder
public class MappingRule {

    /**
     * 注意应和configSet.html中的excel字段保持一致
     */
    public final static String EXCEL_SOURCE_TABLE_NAME = "原表表名";
    public final static String EXCEL_SOURCE_FIELD_NAME = "原表字段名";
    public final static String EXCEL_IS_PRIMARY_KEY = "是否主键";
    public final static String EXCEL_TARGET_TABLE_NAME = "目标表表名";
    public final static String EXCEL_TARGET_FIELD_NAME = "目标表字段名";
    public final static String EXCEL_FIELD_CHECK_METHOD_NAME = "自定义方法";
    public final static String EXCEL_FIELD_CHECK_METHOD_ARGS = "自定义入参";

    @Getter
    @Setter
    private SourceMappingItem sourceMappingItem;

    @Getter
    @Setter
    private TargetMappingItem targetMappingItem;

    /**
     * 自定义source字段和target字段的转换处理逻辑
     * bean需要实现FieldCheckExt的check方法
     */
    @Getter
    @Setter
    private CustomizedMethod fieldCheckMethod;

    /**
     * target表中的值可能和source表无关
     * 例如target字段校验写死值，或者有其他和source表无关的校验方式
     * @return
     */
    public boolean hasSourceTable() {
        return sourceMappingItem != null && StringUtils.isNotEmpty(sourceMappingItem.getTableName());
    }

    /**
     * primary key的mapping rule不需要target表信息
     * @return
     */
    public boolean hasTargetTable() {
        return targetMappingItem != null && StringUtils.isNotEmpty(targetMappingItem.getTableName());
    }
}
