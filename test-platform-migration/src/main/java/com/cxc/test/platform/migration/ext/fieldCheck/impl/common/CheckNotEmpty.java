package com.cxc.test.platform.migration.ext.fieldCheck.impl.common;

import com.cxc.test.platform.common.domain.diff.FieldCheckResult;
import com.cxc.test.platform.migration.ext.fieldCheck.FieldCheckExt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * targetValue不为空就通过
 */
@Component("checkNotEmpty")
public class CheckNotEmpty implements FieldCheckExt {

    @Override
    public FieldCheckResult check(Object sourceValue, Object targetValue, List<Object> args) {
        return FieldCheckResult.builder()
            .isPass(targetValue != null)
            .computedValue(sourceValue)
            .build();
    }
}
