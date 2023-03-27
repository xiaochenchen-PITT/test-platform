package com.cxc.test.platform.datacheck.ext.fieldCheck.impl.common;

import com.cxc.test.platform.common.domain.diff.FieldCheckResult;
import com.cxc.test.platform.datacheck.ext.fieldCheck.FieldCheckExt;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * targetValue不为空就通过
 */
@Component("checkNotEmpty")
public class CheckNotEmpty implements FieldCheckExt {

    @Override
    public FieldCheckResult check(List<Object> sourceValues, Object targetValue, List<Object> args) {
        Object sourceValue = CollectionUtils.isNotEmpty(sourceValues) ? sourceValues.get(0) : null;

        return FieldCheckResult.builder()
                .isPass(targetValue != null)
                .computedValue(sourceValue)
                .build();
    }
}
