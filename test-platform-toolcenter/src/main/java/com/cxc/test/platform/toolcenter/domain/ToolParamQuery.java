package com.cxc.test.platform.toolcenter.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToolParamQuery {

    private Long paramId;

    private Long toolId;

    private String toolParamName;
}
