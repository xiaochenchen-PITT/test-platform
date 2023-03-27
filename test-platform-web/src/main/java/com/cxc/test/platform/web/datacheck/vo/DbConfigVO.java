package com.cxc.test.platform.web.datacheck.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DbConfigVO {

    private String sourceDriverClassName;

    private String sourceDbUrl;

    private String sourceUserName;

    private String sourcePassword;

    private String targetDriverClassName;

    private String targetDbUrl;

    private String targetUserName;

    private String targetPassword;

}
