package com.cxc.test.platform.datacheck.ext.skip.impl;

import com.cxc.test.platform.datacheck.domain.config.DataCheckConfig;
import com.cxc.test.platform.datacheck.domain.SourceData;
import com.cxc.test.platform.datacheck.ext.skip.SkipCheckHandler;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class DataCheckConfigSkipHandler implements SkipCheckHandler {

    @Override
    public String getName() {
        return "dataCheckConfigSkipHandler";
    }

    @Override
    public boolean shouldSkip(String sourceTableName, String sourceFieldName, SourceData sourceData, DataCheckConfig dataCheckConfig) {
        if (StringUtils.isEmpty(sourceTableName) || StringUtils.isEmpty(sourceTableName)) {
            return false;
        }

        // 没有配置，默认都需要校验
        if (dataCheckConfig == null || MapUtils.isEmpty(dataCheckConfig.getTableAndCheckFieldsMap())) {
            return false;
        }

        // 有配置，则只校验配置的表和字段
        Map<String, Set<String>> checkMap = dataCheckConfig.getTableAndCheckFieldsMap();
        boolean shouldCheck = checkMap.containsKey(sourceTableName) && checkMap.get(sourceTableName).contains(sourceFieldName);
        return !shouldCheck;
    }
}
