package com.cxc.test.platform.common.domain.diff;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class DiffDetail {

    private Long batchId;

    private Long configId;

    /**
     * @see DiffTypeConstant
     */
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

    private Date createdTime;

    private Date modifiedTime;
}
