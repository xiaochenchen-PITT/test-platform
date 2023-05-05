package com.cxc.test.platform.web.apicheck.vo;

import lombok.Data;

import java.util.List;

@Data
public class ApiConfigVO {

    // table 1 - source data table
    private String dataSourceType;

    private String sourceDriverClassName;

    private String sourceDbUrl;

    private String sourceUserName;

    private String sourcePassword;

    private String sourceTableName;

    private List<SourceFieldVO> sourceFieldCombo; // jsonarray




    // table 2 - 出入参table
    private String param;

    private Boolean apiCheckSwitch;

    private String apiType;

    private String httpMethod;

    private String httpApi;

    private String rpcApi;

    private List<WhiteListFieldVO> apiWhiteListFieldCombo; // jsonarray

    private Boolean dbCheckSwitch;

    private String checkDriverClassName;

    private String checkDbUrl;

    private String checkUserName;

    private String checkPassword;

    private List<ApiCheckDbCombo> dbCheckCombo; // 单独表 table 3 dbcheck



    // table 4 - 灰度表
    private String grayDecideType;

    private List<HttpGrayVO> httpGrayCombo;

}
