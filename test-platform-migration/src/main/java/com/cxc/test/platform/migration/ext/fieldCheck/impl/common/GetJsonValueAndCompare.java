package com.cxc.test.platform.migration.ext.fieldCheck.impl.common;

import com.alibaba.fastjson.JSONObject;
import com.cxc.test.platform.common.domain.diff.FieldCheckResult;
import com.cxc.test.platform.common.utils.CommonUtils;
import com.cxc.test.platform.common.utils.JsonUtils;
import com.cxc.test.platform.migration.ext.fieldCheck.FieldCheckExt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 通过json path从sourceValue的json串中获取json value，并和targetValue对比
 * 1. sourceValue需要是一个标准的json string格式，
 * 2. json path需要拿到叶子节点，即取到的值需要是基础类型，不能是对象
 */
@Component("getJsonValueAndCompare")
public class GetJsonValueAndCompare implements FieldCheckExt {

    @Override
    public FieldCheckResult check(Object sourceValue, Object targetValue, List<Object> args) {
        String jsonPath = String.valueOf(args.get(0));
        if (!jsonPath.startsWith("$.")) {
            jsonPath = "$." + jsonPath;
        }

        String sourceValueStr = String.valueOf(sourceValue);
        String jsonValue = String.valueOf(JsonUtils.getJsonFromPath(JSONObject.parseObject(sourceValueStr), jsonPath));

        return FieldCheckResult.builder()
            .isPass(CommonUtils.generalEquals(jsonValue, String.valueOf(targetValue)))
            .computedValue(String.valueOf(jsonValue))
            .build();
    }

    public static void main(String[] args) {
        String s = "{ \"store\": {\n" +
            "    \"book\": [ \n" +
            "      { \"category\": \"reference\",\n" +
            "        \"author\": \"Nigel Rees\",\n" +
            "        \"title\": \"Sayings of the Century\",\n" +
            "        \"price\": 8.95\n" +
            "      },\n" +
            "      { \"category\": \"fiction\",\n" +
            "        \"author\": \"Evelyn Waugh\",\n" +
            "        \"title\": \"Sword of Honour\",\n" +
            "        \"price\": 12.99\n" +
            "      },\n" +
            "      { \"category\": \"fiction\",\n" +
            "        \"author\": \"Herman Melville\",\n" +
            "        \"title\": \"Moby Dick\",\n" +
            "        \"isbn\": \"0-553-21311-3\",\n" +
            "        \"price\": 8.99\n" +
            "      },\n" +
            "      { \"category\": \"fiction\",\n" +
            "        \"author\": \"J. R. R. Tolkien\",\n" +
            "        \"title\": \"The Lord of the Rings\",\n" +
            "        \"isbn\": \"0-395-19395-8\",\n" +
            "        \"price\": 22.99\n" +
            "      }\n" +
            "    ],\n" +
            "    \"bicycle\": {\n" +
            "      \"color\": \"red\",\n" +
            "      \"price\": 19.95\n" +
            "    }\n" +
            "  }\n" +
            "}\n";

        System.out.println( JsonUtils.getJsonFromPath(JSONObject.parseObject(s), "$..author"));

    }
}
