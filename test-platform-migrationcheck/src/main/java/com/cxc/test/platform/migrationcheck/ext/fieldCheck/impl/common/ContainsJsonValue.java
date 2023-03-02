package com.cxc.test.platform.migrationcheck.ext.fieldCheck.impl.common;

import com.alibaba.fastjson.JSONObject;
import com.cxc.test.platform.common.domain.diff.FieldCheckResult;
import com.cxc.test.platform.common.utils.CommonUtils;
import com.cxc.test.platform.common.utils.JsonUtils;
import com.cxc.test.platform.migrationcheck.ext.fieldCheck.FieldCheckExt;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 判断targetValue是否包含sourceValue
 * 1. targetValue需要为标准json格式
 * 2. sourceValue需要是基础类型，不能是对象
 */
@Component("containsJsonValue")
public class ContainsJsonValue implements FieldCheckExt {

    @Override
    public FieldCheckResult check(List<Object> sourceValues, Object targetValue, List<Object> args) {
        Object sourceValue = CollectionUtils.isNotEmpty(sourceValues) ? sourceValues.get(0) : null;

        String key = String.valueOf(args.get(0));
        String targetValueStr = String.valueOf(targetValue);

        String jsonValue = String.valueOf(JsonUtils.getJsonFromPath(JSONObject.parseObject(targetValueStr), "$." + key));

        return FieldCheckResult.builder()
                .isPass(CommonUtils.generalEquals(String.valueOf(sourceValue), jsonValue))
                .computedValue(sourceValue)
                .build();
    }
}
