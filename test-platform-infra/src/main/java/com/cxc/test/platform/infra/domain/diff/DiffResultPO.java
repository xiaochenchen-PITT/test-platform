package com.cxc.test.platform.infra.domain.diff;

import com.cxc.test.platform.common.domain.diff.TaskStatusConstant;
import lombok.Data;

import java.util.Date;

@Data
public class DiffResultPO {

    /**
     * 主键id，无实际业务意义
     */
    private Long id;

    private Long batchId;

    private Long configId;

    /**
     * 1：成功，0：失败
     */
    private int isSuccess;

    /**
     * 1：相等，0：不相等
     */
    private int isEqual;

    /**
     * @see TaskStatusConstant
     */
    private String status;

    /**
     * 运行进展，百分制例如25.56%（保留2位小数）
     */
    private String progress;

    /**
     * 运行者，保留，暂时没用
     */
    private String runner;

    private String errorMessage;

    private String triggerUrl;

    private Long totalCount;

    private Long failedCount;

    /**
     * 扩展字段，json格式
     */
    private String features;

    private Date createdTime;

    private Date modifiedTime;
}
