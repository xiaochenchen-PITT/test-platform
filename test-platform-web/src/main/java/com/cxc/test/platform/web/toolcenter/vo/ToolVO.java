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

    private String bean;

    private String url;

    private String status;

    private String creator;

    private String domain;
}
