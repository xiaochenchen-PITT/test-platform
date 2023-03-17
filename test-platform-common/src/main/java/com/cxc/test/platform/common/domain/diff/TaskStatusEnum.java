package com.cxc.test.platform.common.domain.diff;

import lombok.Getter;

public enum TaskStatusEnum {

    NOT_STARTED("not_started", "未开始"),

    RUNNING("running", "运行中"),

    FINISHED("finished", "已结束"),

    FAILED("failed", "失败");

    @Getter
    private String status;

    @Getter
    private String label;

    TaskStatusEnum(String status, String label) {
        this.status = status;
        this.label = label;
    }

    public static TaskStatusEnum getByStatus(String status) {
        for (TaskStatusEnum taskStatusEnum : TaskStatusEnum.values()) {
            if (taskStatusEnum.getStatus().equalsIgnoreCase(status)) {
                return taskStatusEnum;
            }
        }

        return null;
    }


}
