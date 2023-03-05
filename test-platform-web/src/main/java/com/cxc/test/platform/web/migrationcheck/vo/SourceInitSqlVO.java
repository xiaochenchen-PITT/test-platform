package com.cxc.test.platform.web.migrationcheck.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceInitSqlVO {

    private Long id;

    private Long configId;

    private String sourceTableName;

    private String initSql;
}
