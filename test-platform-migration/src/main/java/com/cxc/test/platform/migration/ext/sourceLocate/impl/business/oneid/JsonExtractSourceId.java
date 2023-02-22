package com.cxc.test.platform.migration.ext.sourceLocate.impl.business.oneid;

import com.cxc.test.platform.migration.ext.sourceLocate.SourceLocateExt;
import com.cxc.test.platform.migration.ext.sourceLocate.impl.GetJsonAndLocate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Component("jsonExtractSourceId")
public class JsonExtractSourceId implements SourceLocateExt {

    @Resource
    GetJsonAndLocate getJsonAndLocate;

    private final String KEY = "sourceId";

    @Override
    public String locateSource(String locateField, String sourceTable, String sourceId, List<Object> args) {
        String jsonPath = "$." + sourceTable + "_" + KEY;

        args = Arrays.asList(jsonPath);
        return getJsonAndLocate.locateSource(locateField, sourceTable, sourceId, args);
    }
}
