package com.cxc.test.platform.migrationcheck.ext.sourceLocate;

import java.util.List;

public interface SourceLocateExt {

    /**
     * 生成在targetSql中唯一定位sourceId的where语句，例如biz_feature like "%sourceId:1234%"
     *
     * @param locateField 目标表中承载sourceId的的字段，一般来说是features
     * @param sourceTable 原表名
     * @param sourceId 原表的主键id
     * @param args 辅助定位的参数
     *
     * @return 在targetSql中唯一定位source数据的where语句，例如biz_feature like "%sourceId:1234%"
     */
    String locateSource(String locateField, String sourceTable, String sourceId, List<Object> args);
}
