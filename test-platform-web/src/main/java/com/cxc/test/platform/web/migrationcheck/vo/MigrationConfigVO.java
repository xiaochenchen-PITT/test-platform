package com.cxc.test.platform.web.migrationcheck.vo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class MigrationConfigVO {

    private String sourceDriverClassName;

    private String sourceDbUrl;

    private String sourceUserName;

    private String sourcePassword;

    private String targetDriverClassName;

    private String targetDbUrl;

    private String targetUserName;

    private String targetPassword;

    /**
     * 字段映射关系
     */
    private JSONArray excel;

    /**
     * 目标表中，原表主键id定位方法
     * key: 目标表名
     */
    private JSONObject targetLocatorKvs;

    /**
     * 源数据的数据初始化sql
     * key: 源表名
     */
    private JSONObject sourceTableSqlKvs;

}
