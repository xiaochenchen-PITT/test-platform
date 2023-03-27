package com.cxc.test.platform.web.migrationcheck.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiffResultVO {

    private Long batchId;

    private Long configId;

    private String isSuccess;

    private String isEqual;

    private String status;

    private String progress;

    private String runner;

    private String errorMessage;

    private String triggerUrl;

    private Long totalCount;

    private Long failedCount;

    private String runningIp;
}
