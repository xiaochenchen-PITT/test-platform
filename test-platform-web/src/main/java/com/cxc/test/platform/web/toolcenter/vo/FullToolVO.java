package com.cxc.test.platform.web.toolcenter.vo;

import com.alibaba.fastjson.JSONArray;
import lombok.Builder;
import lombok.Data;

/**
 * 新增专用，带工具参数
 */
@Data
@Builder
public class FullToolVO {

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

    private JSONArray paramCombo;
}
