package com.cxc.test.platform.infra.domain.diff;

import lombok.Builder;
import lombok.Data;

import java.sql.Date;

@Data
@Builder
public class DiffResultPO {

    private Long batchId;

    /**
     * 1：成功，0：失败
     */
    private int isSuccess;

    /**
     * 1：相等，0：不相等
     */
    private int isEqual;

    private String errorMessage;

    private String triggerUrl;

    private Long totalCount;

    private Long failedCount;

    private Date createdTime;

    private Date modifiedTime;
}
