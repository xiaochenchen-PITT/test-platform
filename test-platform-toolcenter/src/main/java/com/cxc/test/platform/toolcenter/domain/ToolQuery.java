package com.cxc.test.platform.toolcenter.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToolQuery {

    private Long id;

    private Long toolId;

    private String toolName;

    private String toolType;

    private boolean isQueryDeleted = false;
}
