package com.cxc.test.platform.infra.domain.migrationcheck;

import lombok.Builder;
import lombok.Data;

import java.sql.Date;

@Data
@Builder
public class SourceInitSqlPO {

    private Long configId;

    private String sourceTableName;

    private String initSql;

    private Date createdTime;

    private Date modifiedTime;
}
