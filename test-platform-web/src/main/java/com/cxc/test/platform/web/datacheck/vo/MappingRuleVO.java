package com.cxc.test.platform.web.datacheck.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MappingRuleVO {

    private Long id;

    private Long configId;

    private String sourceTableName;

    private String sourceFieldNames;

    private Boolean isPrimaryKey;

    private String targetTableName;

    private String targetFieldName;

    private String fieldCheckMethodName;

    private String fieldCheckMethodArgs;

}
