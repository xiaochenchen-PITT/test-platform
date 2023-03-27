package com.cxc.test.platform.datacheck.ext.fieldCheck.impl.common;

import com.cxc.test.platform.common.domain.diff.FieldCheckResult;
import com.cxc.test.platform.common.utils.CommonUtils;
import com.cxc.test.platform.datacheck.ext.fieldCheck.FieldCheckExt;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 0 = false; 1 = true; null = false
 */
@Component("booleanCompare")
public class BooleanCompare implements FieldCheckExt {

    @Override
    public FieldCheckResult check(List<Object> sourceValues, Object targetValue, List<Object> args) {
        Object sourceValue = CollectionUtils.isNotEmpty(sourceValues) ? sourceValues.get(0) : null;

        return FieldCheckResult.builder()
                .isPass(CommonUtils.sameStringInPool(String.valueOf(sourceValue), String.valueOf(targetValue)))
                .computedValue(sourceValue)
                .build();
    }
}
