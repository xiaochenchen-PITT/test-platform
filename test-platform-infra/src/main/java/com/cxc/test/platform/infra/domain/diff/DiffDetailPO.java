package com.cxc.test.platform.infra.domain.diff;

import lombok.Builder;
import lombok.Data;

import java.sql.Date;

@Data
@Builder
public class DiffDetailPO {

    private Long batchId;

    /**
     * @see com.cxc.test.platform.common.domain.diff.DiffTypeConstant
     */
    private String diffType;

    private String sourceQuerySql;

    private String sourceTableName;

    private String sourceFieldName;

    private String sourceValue;

    private String computedSourceValue;

    private String targetQuerySql;

    private String targetTableName;

    private String targetFieldName;

    private String targetValue;

    private String errorMessage;

    private Date createdTime;

    private Date modifiedTime;
}
