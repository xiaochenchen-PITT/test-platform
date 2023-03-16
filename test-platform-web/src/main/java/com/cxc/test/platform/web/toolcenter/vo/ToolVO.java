package com.cxc.test.platform.web.toolcenter.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToolVO {

    private Long toolId;

    private String name;

    private String desc;

    private String type;

    private String beanName;

    /**
     * Java类型和Http类型的汇总展示，同时包含beanClass/url和method，例如：
     * Java类型：com.cxc.test.platform.toolcenter.service.ToolCenterService#demoRun
     * Http类型：get#127.0.0.1:8080/toolcenter/get_tree_select
     */
    private String api;

    private String status;

    private String creator;

    private String domain;

    private Long totalCount;

    private Long successCount;

    private String rank;
}
