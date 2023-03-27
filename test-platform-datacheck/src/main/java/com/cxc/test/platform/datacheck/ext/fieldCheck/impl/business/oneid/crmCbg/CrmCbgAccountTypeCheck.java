package com.cxc.test.platform.datacheck.ext.fieldCheck.impl.business.oneid.crmCbg;

import com.cxc.test.platform.common.domain.diff.FieldCheckResult;
import com.cxc.test.platform.common.utils.CommonUtils;
import com.cxc.test.platform.datacheck.ext.fieldCheck.FieldCheckExt;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 将account_type进行转换
 * oneid项目，CBG客户数据迁移专用
 */
@Component("crmCbgAccountTypeCheck")
public class CrmCbgAccountTypeCheck implements FieldCheckExt {

    @Override
    public FieldCheckResult check(List<Object> sourceValues, Object targetValue, List<Object> args) {
        Object sourceValue = CollectionUtils.isNotEmpty(sourceValues) ? sourceValues.get(0) : null;
        String parsed = convert(String.valueOf(sourceValue));

        return FieldCheckResult.builder()
                .isPass(CommonUtils.generalEquals(parsed, targetValue))
                .computedValue(parsed)
                .build();
    }

    private String convert(String sourceValue) {
        if (StringUtils.equals(sourceValue, "1")) {
            return "2";
        } else if (StringUtils.equals(sourceValue, "2")) {
            return "1";
        } else {
            return "3";
        }
    }
}
