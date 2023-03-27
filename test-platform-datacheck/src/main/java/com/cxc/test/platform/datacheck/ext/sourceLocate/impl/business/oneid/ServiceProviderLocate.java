package com.cxc.test.platform.datacheck.ext.sourceLocate.impl.business.oneid;

import com.cxc.test.platform.datacheck.domain.SourceData;
import com.cxc.test.platform.datacheck.ext.sourceLocate.SourceLocateExt;
import com.cxc.test.platform.datacheck.ext.sourceLocate.impl.GetJsonAndLocate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 不同的原表在对应的目标表中要加上不同的service_type = x
 * oneid项目，CBG物流服务商迁移专用
 */
@Component("serviceProviderLocate")
public class ServiceProviderLocate implements SourceLocateExt {

    @Resource
    GetJsonAndLocate getJsonAndLocate;

    private final String KEY = "sourceId";

    @Override
    public String locateSource(String locateField, SourceData sourceData, String sourceId, List<Object> args) {
        String sourceTable = sourceData.getTableName();
        String jsonPath = "$." + sourceTable + "_" + KEY;

        args = Arrays.asList(jsonPath);
        String jsonFilter = getJsonAndLocate.locateSource(locateField, sourceData, sourceId, args);

        String serviceTypeFilter = String.format("service_type = \"%s\"", getServiceTypeFromSourceTable(sourceTable));

        return jsonFilter + " and " + serviceTypeFilter;
    }

    private String getServiceTypeFromSourceTable(String sourceTable) {
        if (sourceTable.equalsIgnoreCase("t_merchants_service_overseas")) {
            return "10";
        } else if (sourceTable.equalsIgnoreCase("t_merchants_service_inland")) {
            return "7";
        } else if (sourceTable.equalsIgnoreCase("t_merchants_service_clearance")) {
            return "8";
        } else if (sourceTable.equalsIgnoreCase("t_merchants_service_agency")) {
            return "9";
        } else {
            Assert.isTrue(false, "source table:" + sourceTable + " should not use this serviceProviderLocate");
        }

        return null;
    }
}
