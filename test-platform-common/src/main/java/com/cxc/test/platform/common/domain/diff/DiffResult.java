package com.cxc.test.platform.common.domain.diff;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DiffResult {

    private Long batchId;

    private Long configId;

    private Boolean isSuccess;

    private Boolean isEqual;

    /**
     * @see TaskStatusEnum 的status
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

    private List<DiffDetail> diffDetailList;

    private String errorMessage;

    private String triggerUrl;

    private Long totalCount;

    private Long failedCount;

    private Map<String, String> features = new HashMap<>();

    private Date createdTime;

    private Date modifiedTime;

    public List<DiffDetail> getDiffDetailList() {
        if (diffDetailList == null) {
            diffDetailList = new ArrayList<>();
        }

        return diffDetailList;
    }

    public Map<String, String> getFeatures() {
        if (features == null) {
            features = new HashMap<>();
        }

        return features;
    }

    public String getFeatureByKey(String key) {
        if (features == null) {
            features = new HashMap<>();
        }

        return features.get(key);
    }

    public Map<String, String> addOrUpdateFeature(String key, String value) {
        if (features == null) {
            return new HashMap<>();
        }

        features.put(key, value);
        return features;
    }
}
