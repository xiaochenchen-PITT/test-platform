package com.cxc.test.platform.migrationcheck.ext.sourceLocate.impl;

import com.cxc.test.platform.migrationcheck.domain.MigrationData;
import com.cxc.test.platform.migrationcheck.ext.sourceLocate.SourceLocateExt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * sourceId信息存储在target表中的一个json字段中
 */
@Component("getJsonAndLocate")
public class GetJsonAndLocate implements SourceLocateExt {

    @Override
    public String locateSource(String locateField, MigrationData sourceData, String sourceId, List<Object> args) {
        String jsonPath = String.valueOf(args.get(0));
        if (!jsonPath.startsWith("$.")) {
            jsonPath = "$." + jsonPath;
        }

        return String.format("json_extract(%s, \"%s\") = \"%s\"", locateField, jsonPath, sourceId);
    }
}
