package com.cxc.test.platform.common.domain.diff;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JsonDiffField {

    private String jsonPath;

    private String leftValue;

    private String rightValue;
}
