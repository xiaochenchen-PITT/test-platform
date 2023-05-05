package com.cxc.test.platform.common.domain.diff;

import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

@Data
public class BatchRunningBundle {

    private Long batchId;

    private boolean isRunning;

    private DiffResult diffResult;

    private AtomicLong count = new AtomicLong(0);

    private AtomicLong failedCount = new AtomicLong(0);

}
