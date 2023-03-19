package com.cxc.test.platform.web.toolcenter.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToolParamVO {

    private Long paramId;

    private Long toolId;

    private String name;

    private String label;

    private String desc;

    private String paramClass;

    private Boolean isRequired;

    private Boolean hasDefault;

    private String defaultValue;

    private String inputType;

    private String optionValues;
}
