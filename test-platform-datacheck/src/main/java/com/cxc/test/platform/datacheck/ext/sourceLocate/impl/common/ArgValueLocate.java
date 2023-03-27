package com.cxc.test.platform.datacheck.ext.sourceLocate.impl.common;

import com.cxc.test.platform.datacheck.domain.SourceData;
import com.cxc.test.platform.datacheck.ext.sourceLocate.SourceLocateExt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("argValueLocate")
public class ArgValueLocate implements SourceLocateExt {

    @Override
    public String locateSource(String locateField, SourceData sourceData, String sourceId, List<Object> args) {
        String value = String.valueOf(args.get(0));

        return String.format("%s = \"%s\"", locateField, value);
    }
}