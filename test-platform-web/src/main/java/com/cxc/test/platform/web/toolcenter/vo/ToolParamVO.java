package com.cxc.test.platform.web.toolcenter.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToolParamVO {

    private Long paramId;

    private Long toolId;

    private String name;

    private String desc;

    private String paramClass;

    private String isRequired;

    private String hasDefault;

    private String defaultValue;

    private String inputType;

    private String optionValues;
}
