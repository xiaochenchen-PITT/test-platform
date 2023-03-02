package com.cxc.test.platform.infra.domain.migrationcheck;

import lombok.Builder;
import lombok.Data;

import java.sql.Date;

@Data
@Builder
public class MappingRulePO {

    private Long configId;

    private String sourceTableName;

    private String sourceFieldNames;

    /**
     * 1：是，0：否
     */
    private int isPrimaryKey;

    private String targetTableName;

    private String targetFieldName;

    /**
     * 自定义source字段和target字段的转换处理逻辑
     */
    private String fieldCheckMethodName;

    /**
     * 自定义source字段和target字段的转换处理逻辑的入参，逗号分隔
     */
    private String fieldCheckMethodArgs;

    private Date createdTime;

    private Date modifiedTime;
}
