package com.cxc.test.platform.migrationcheck.ext.sourceLocate.impl.common;

import com.cxc.test.platform.migrationcheck.domain.MigrationData;
import com.cxc.test.platform.migrationcheck.ext.sourceLocate.SourceLocateExt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("argValueLocate")
public class ArgValueLocate implements SourceLocateExt {

    @Override
    public String locateSource(String locateField, MigrationData sourceData, String sourceId, List<Object> args) {
        String value = String.valueOf(args.get(0));

        return String.format("%s = \"%s\"", locateField, value);
    }
}