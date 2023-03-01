package com.cxc.test.platform.migrationcheck.ext.fieldCheck.impl.common;

import com.cxc.test.platform.common.domain.diff.FieldCheckResult;
import com.cxc.test.platform.migrationcheck.ext.fieldCheck.FieldCheckExt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 将sourceValue抬高一定数量，并和targetValue进行对比
 * sourceValue和targetValue需要是id类型
 */
@Component("idIncreaseAndCompare")
public class IdIncreaseAndCompare implements FieldCheckExt {

    @Override
    public FieldCheckResult check(Object sourceValue, Object targetValue, List<Object> args) {
        Long num = Long.valueOf(String.valueOf(args.get(0)));
        Long computedValue = Long.valueOf(String.valueOf(sourceValue) + num);

        return FieldCheckResult.builder()
            .isPass(Objects.equals(computedValue, Long.valueOf(String.valueOf(targetValue))))
            .computedValue(computedValue)
            .build();
    }
}
