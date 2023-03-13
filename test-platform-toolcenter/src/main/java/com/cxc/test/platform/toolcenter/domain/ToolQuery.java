package com.cxc.test.platform.toolcenter.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToolQuery {

    private Long id;

    private Long toolId;

    private String name;

    private String type;

    private String status;

    private String creator;

    private String domain;
}
