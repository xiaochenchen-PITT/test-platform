package com.cxc.test.platform.migrationcheck.ext.fieldCheck.impl.business.oneid.crmCbg;

import com.cxc.test.platform.common.domain.diff.FieldCheckResult;
import com.cxc.test.platform.common.utils.CommonUtils;
import com.cxc.test.platform.migrationcheck.ext.fieldCheck.FieldCheckExt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 将account_type进行转换
 * oneid项目，CBG客户数据迁移专用
 */
@Component("crmCbgAccountTypeCheck")
public class CrmCbgAccountTypeCheck implements FieldCheckExt {

    @Override
    public FieldCheckResult check(Object sourceValue, Object targetValue, List<Object> args) {
        String parsed = convert(String.valueOf(sourceValue));

        return FieldCheckResult.builder()
            .isPass(CommonUtils.generalEquals(parsed, targetValue))
            .computedValue(parsed)
            .build();
    }

    private String convert(String sourceValue) {
        if (sourceValue.equals("1")) {
            return "2";
        } else if (sourceValue.equals("2")) {
            return "1";
        } else {
            return "3";
        }
    }
}
