package com.cxc.test.platform.infra.domain.migrationcheck;

import lombok.Builder;
import lombok.Data;

import java.sql.Date;

@Data
@Builder
public class MigrationConfigPO {

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
