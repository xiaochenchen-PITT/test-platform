package com.cxc.test.platform.web.datacheck.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataCheckRunningItemVO {

    private Long batchId;

    private Long configId;

    private Long mappingRuleCount;

    private String status;

    /**
     * 例如20.25%
     */
    private String progress;

    private String runner;

    /**
     * 任务总数 / 失败总数
     */
    private String taskCount;

    /**
     * IP[:端口]
     */
    private String runningIp;

    private String createdTime;

    private String modifiedTime;
}
