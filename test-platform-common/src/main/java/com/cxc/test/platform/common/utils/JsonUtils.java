package com.cxc.test.platform.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {

    public static Object getJsonFromPath(JSONObject sourceJO, String jsonPath) {
        try {
            Object obj = JsonPath.read(sourceJO, jsonPath);
            return obj;
        } catch (PathNotFoundException e) {
            log.error("Did not find result from json path: " + jsonPath, e);
        }

        return null;
    }

}
