package com.cxc.test.platform.migrationcheck.ext.fieldCheck.impl.common;

import com.cxc.test.platform.common.domain.diff.FieldCheckResult;
import com.cxc.test.platform.common.utils.JsonCompareUtils;
import com.cxc.test.platform.migrationcheck.ext.fieldCheck.FieldCheckExt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * json平迁对比
 * sourceValur和targetValue需要为标准json格式
 */
@Component("jsonCompare")
public class JsonCompare implements FieldCheckExt {

    @Override
    public FieldCheckResult check(Object sourceValue, Object targetValue, List<Object> args) {
        return FieldCheckResult.builder()
            .isPass(JsonCompareUtils.compareJson(String.valueOf(sourceValue), String.valueOf(targetValue), null).getIsEqual())
//            .isPass(checkEachFeaturePair(Arrays.asList(sourceValue.split(";")), Arrays.asList(targetValue.split(";"))))
            .computedValue(null)
            .build();
    }

//    // 以下是【feature对需要使用;分割，同时使用:链接key和value】格式的逻辑
//    @Deprecated
//    private boolean checkEachFeaturePair(List<String> sourceFeaturePairList, List<String> targetFeaturePairList) {
//        for (String sourceFeaturePair : sourceFeaturePairList) {
//            // 非法的feature pair
//            if (!sourceFeaturePair.contains(":")) {
//                continue;
//            }
//
////            String sourceFeatureKey = sourceFeaturePair.split(":")[0];
////            String sourceFeatureValue = sourceFeaturePair.split(":")[1];
//
//            if (!targetFeaturePairList.contains(sourceFeaturePair)) {
//                return false;
//            }
//        }
//
//        return true;
//    }
}
