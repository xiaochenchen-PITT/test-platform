package com.cxc.test.platform.infra.domain.diff;

import lombok.Data;

import java.util.Date;

@Data
public class DiffDetailPO {

    private Long batchId;

    private Long configId;

    /**
     * @see com.cxc.test.platform.common.domain.diff.DiffTypeConstant
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
