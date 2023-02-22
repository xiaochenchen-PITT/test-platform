package com.cxc.test.platform.common.domain.diff;

import lombok.Builder;
import lombok.Data;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class DiffResult {

    private Long batchId;

    private Boolean isSuccess;

    private Boolean isEqual;

    private List<DiffDetail> diffDetailList;

    private String errorMessage;

    private String triggerUrl;

    private Long totalCount;

    private Long failedCount;

    private Date createdTime;

    private Date modifiedTime;

    public List<DiffDetail> getDiffDetailList() {
        if (diffDetailList == null) {
            diffDetailList = new ArrayList<>();
        }

        return diffDetailList;
    }
}
