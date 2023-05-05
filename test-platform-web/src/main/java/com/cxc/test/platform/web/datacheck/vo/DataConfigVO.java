package com.cxc.test.platform.web.datacheck.vo;

import com.alibaba.fastjson.JSONArray;
import com.cxc.test.platform.datacheck.domain.mapping.MappingRule;
import lombok.Data;

import java.util.List;

@Data
public class DataConfigVO {

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
     * 通过前端excel解析得出
     * @see MappingRule 中为key
     */
    private JSONArray excel;

    /**
     * 目标表中，原表主键id定位方法
     * key: 目标表名
     */
    private List<SourceLocatorVO> locatorCombo;

    /**
     * 源数据的数据初始化sql
     * key: 源表名
     */
    private List<SourceInitSqlVO> initSqlCombo;

}
