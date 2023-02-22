package com.cxc.test.platform.migration.ext.skip.impl;

import com.cxc.test.platform.migration.domain.config.MigrationCheckConfig;
import com.cxc.test.platform.migration.domain.data.MigrationData;
import com.cxc.test.platform.migration.ext.skip.SkipCheckHandler;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class MigrationCheckConfigSkipHandler implements SkipCheckHandler {

    @Override
    public String getName() {
        return "migrationCheckConfigSkipHandler";
    }

    @Override
    public boolean shouldSkip(String sourceTableName, String sourceFieldName, MigrationData sourceData, MigrationCheckConfig migrationCheckConfig) {
        if (StringUtils.isEmpty(sourceTableName) || StringUtils.isEmpty(sourceTableName)) {
            return false;
        }

        // 没有配置，默认都需要校验
        if (migrationCheckConfig == null || MapUtils.isEmpty(migrationCheckConfig.getTableAndCheckFieldsMap())) {
            return false;
        }

        // 有配置，则只校验配置的表和字段
        Map<String, Set<String>> checkMap = migrationCheckConfig.getTableAndCheckFieldsMap();
        boolean shouldCheck = checkMap.containsKey(sourceTableName) && checkMap.get(sourceTableName).contains(sourceFieldName);
        return !shouldCheck;
    }
}
