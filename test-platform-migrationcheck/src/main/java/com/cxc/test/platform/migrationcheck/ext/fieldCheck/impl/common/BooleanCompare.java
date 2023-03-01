package com.cxc.test.platform.migrationcheck.ext.fieldCheck.impl.common;

import com.cxc.test.platform.common.domain.diff.FieldCheckResult;
import com.cxc.test.platform.common.utils.CommonUtils;
import com.cxc.test.platform.migrationcheck.ext.fieldCheck.FieldCheckExt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 0 = false; 1 = true; null = false
 */
@Component("booleanCompare")
public class BooleanCompare implements FieldCheckExt {

    @Override
    public FieldCheckResult check(Object sourceValue, Object targetValue, List<Object> args) {
        return FieldCheckResult.builder()
            .isPass(CommonUtils.sameStringInPool(String.valueOf(sourceValue), String.valueOf(targetValue)))
            .computedValue(sourceValue)
            .build();
    }
}
