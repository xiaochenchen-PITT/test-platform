package com.cxc.test.platform.migrationcheck.ext.skip;


import com.cxc.test.platform.migrationcheck.domain.config.MigrationCheckConfig;
import com.cxc.test.platform.migrationcheck.domain.MigrationData;

public interface SkipCheckHandler {

    String getName();

    /**
     * 是否跳过检查
     * 为了统一skip check的逻辑，sourceFieldName和sourceData可能为null
     *
     * @param sourceTableName 原表名，可能为null
     * @param sourceFieldName 原表字段，可能为null
     * @param sourceData 原表的数据，可能为null
     * @param migrationCheckConfig 校验配置，可能为null
     * @return
     */
    boolean shouldSkip(String sourceTableName, String sourceFieldName, MigrationData sourceData, MigrationCheckConfig migrationCheckConfig);
}
