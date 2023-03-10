package com.cxc.test.platform.migrationcheck.ext.fieldCheck;

import com.cxc.test.platform.common.domain.diff.FieldCheckResult;
import java.util.List;

public interface FieldCheckExt {

    /**
     * 将字段迁移前的值（sourceValue）和迁移后的值（targetValue）进行对比/检查
     * @param sourceValues 字段迁移前的值
     * @param targetValue 字段迁移后的值
     * @param args 自定义处理逻辑的入参
     * @return
     */
    FieldCheckResult check(List<Object> sourceValues, Object targetValue, List<Object> args);
}
