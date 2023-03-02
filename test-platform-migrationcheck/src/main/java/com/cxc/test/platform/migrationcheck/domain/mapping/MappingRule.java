package com.cxc.test.platform.migrationcheck.domain.mapping;

import com.cxc.test.platform.migrationcheck.domain.CustomizedMethod;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public class MappingRule {

    /**
     * 注意应和configSet.html中的excel字段保持一致
     */
    public final static String SOURCE_TABLE_NAME = "原表表名";
    public final static String SOURCE_FIELD_NAME = "原表字段名";
    public final static String IS_PRIMARY_KEY = "是否是主键";
    public final static String TARGET_TABLE_NAME = "目标表表名";
    public final static String TARGET_FIELD_NAME = "目标表字段名";
    public final static String FIELD_CHECK_METHOD_NAME = "自定义处理逻辑";
    public final static String FIELD_CHECK_METHOD_ARGS = "自定义处理逻辑入参";

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
}
