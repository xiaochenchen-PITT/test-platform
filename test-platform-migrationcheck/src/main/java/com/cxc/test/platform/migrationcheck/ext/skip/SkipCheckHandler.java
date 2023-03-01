package com.cxc.test.platform.migrationcheck.ext.skip;


import com.cxc.test.platform.migrationcheck.domain.config.MigrationCheckConfig;
import com.cxc.test.platform.migrationcheck.domain.data.MigrationData;

public interface SkipCheckHandler {

    String getName();

    /**
     * 是否跳过检查
     * 为了统一skip check的逻辑，sourceFieldName和sourceData可能分别为null
     *
     * @param sourceTableName
     * @param sourceFieldName 可能为null
     * @param sourceData 可能为null
     * @param migrationCheckConfig 可能为null
     * @return
     */
    boolean shouldSkip(String sourceTableName, String sourceFieldName, MigrationData sourceData, MigrationCheckConfig migrationCheckConfig);
}
