package com.cxc.test.platform.migrationcheck.domain.mapping;

import com.cxc.test.platform.migrationcheck.domain.CustomizedMethod;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public class MappingRule {

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
