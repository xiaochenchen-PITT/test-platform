package com.cxc.test.platform.datacheck.ext.fieldCheck.impl.common;

import com.alibaba.fastjson.JSONObject;
import com.cxc.test.platform.common.domain.diff.FieldCheckResult;
import com.cxc.test.platform.common.utils.JsonCompareUtils;
import com.cxc.test.platform.common.utils.JsonUtils;
import com.cxc.test.platform.datacheck.ext.fieldCheck.FieldCheckExt;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 判断targetValue是否包含sourceValue
 * 1. targetValue和sourceValue需要为标准json格式
 * 2. 一级key暂不支持json array...
 */
@Component("containsJsonObject")
public class ContainsJsonObject implements FieldCheckExt {

    @Override
    public FieldCheckResult check(List<Object> sourceValues, Object targetValue, List<Object> args) {
        Object sourceValue = CollectionUtils.isNotEmpty(sourceValues) ? sourceValues.get(0) : null;

        boolean isPass = true;
        for (Map.Entry<String, Object> entry : JSONObject.parseObject(String.valueOf(sourceValue)).entrySet()) {
            JSONObject sourceJO = JSONObject.parseObject(String.valueOf(sourceValue)).getJSONObject(entry.getKey());
            JSONObject targetJO = (JSONObject) JsonUtils.getJsonFromPath(JSONObject.parseObject(String.valueOf(targetValue)), "$." + entry.getKey());

            isPass = isPass & JsonCompareUtils.compareJson(String.valueOf(sourceJO), String.valueOf(targetJO), null).getIsEqual();
        }

        return FieldCheckResult.builder()
                .isPass(isPass)
                .computedValue(targetValue)
                .build();
    }
}
