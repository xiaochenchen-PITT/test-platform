package com.cxc.test.platform.infra.domain.migrationcheck;

import lombok.Data;

import java.util.Date;

@Data
public class MappingRulePO {

    /**
     * 主键id，无实际业务意义
     */
    private Long id;

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
