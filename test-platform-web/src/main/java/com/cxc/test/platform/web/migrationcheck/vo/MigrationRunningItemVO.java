package com.cxc.test.platform.web.migrationcheck.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MigrationRunningItemVO {

    private Long batchId;

    private Long configId;

    private Long mappingRuleCount;

    private String status;

    private String progress;

    private String runner;

    private Long totalTaskCount;

    private Long failedTaskCount;

    private String createdTime;

    private String modifiedTime;
}
