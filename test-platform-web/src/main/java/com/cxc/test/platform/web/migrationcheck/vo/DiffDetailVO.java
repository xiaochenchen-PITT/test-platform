package com.cxc.test.platform.web.migrationcheck.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiffDetailVO {

    private Long id;

    private Long batchId;

    private Long configId;

    private String diffType;

    private String sourceQuery;

    private String sourceTableName;

    private String sourceFieldName;

    private String sourceValue;

    private String computedSourceValue;

    private String targetQuery;

    private String targetTableName;

    private String targetFieldName;

    private String targetValue;

    private String errorMessage;
}
