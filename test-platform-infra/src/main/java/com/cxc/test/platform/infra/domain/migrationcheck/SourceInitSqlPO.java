package com.cxc.test.platform.infra.domain.migrationcheck;

import lombok.Data;

import java.util.Date;

@Data
public class SourceInitSqlPO {

    /**
     * 主键id，无实际业务意义
     */
    private Long id;

    private Long configId;

    private String sourceTableName;

    private String initSql;

    private Date createdTime;

    private Date modifiedTime;
}
