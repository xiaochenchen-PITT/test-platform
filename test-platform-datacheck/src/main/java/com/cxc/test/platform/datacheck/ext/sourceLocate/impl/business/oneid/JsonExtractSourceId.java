package com.cxc.test.platform.datacheck.ext.sourceLocate.impl.business.oneid;

import com.cxc.test.platform.datacheck.domain.SourceData;
import com.cxc.test.platform.datacheck.ext.sourceLocate.SourceLocateExt;
import com.cxc.test.platform.datacheck.ext.sourceLocate.impl.GetJsonAndLocate;
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
    public String locateSource(String locateField, SourceData sourceData, String sourceId, List<Object> args) {
        String jsonPath = "$." + sourceData.getTableName() + "_" + KEY;

        args = Arrays.asList(jsonPath);
        return getJsonAndLocate.locateSource(locateField, sourceData, sourceId, args);
    }
}
