package com.cxc.test.platform.migrationcheck.ext.fieldCheck.impl.common;

import com.cxc.test.platform.common.domain.diff.FieldCheckResult;
import com.cxc.test.platform.migrationcheck.ext.fieldCheck.FieldCheckExt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * targetValue为一个写死的值
 */
@Component("hardCodeCheck")
public class HardCodeCheck implements FieldCheckExt {

    @Override
    public FieldCheckResult check(List<Object> sourceValues, Object targetValue, List<Object> args) {
        String hardCode = String.valueOf(args.get(0));

        return FieldCheckResult.builder()
                .isPass(StringUtils.equals(hardCode, String.valueOf(targetValue)))
                .computedValue(null)
                .build();
    }
}