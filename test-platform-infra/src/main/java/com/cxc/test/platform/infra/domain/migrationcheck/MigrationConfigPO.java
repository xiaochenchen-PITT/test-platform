package com.cxc.test.platform.infra.domain.migrationcheck;

import lombok.Data;

import java.util.Date;

@Data
public class MigrationConfigPO {

    /**
     * 主键id，无实际业务意义
     */
    private Long id;

    private Long configId;

    private String sourceDriverClassName;

    private String sourceDbUrl;

    private String sourceUserName;

    private String sourcePassword;

    private String targetDriverClassName;

    private String targetDbUrl;

    private String targetUserName;

    private String targetPassword;

    private Date createdTime;

    private Date modifiedTime;
}
