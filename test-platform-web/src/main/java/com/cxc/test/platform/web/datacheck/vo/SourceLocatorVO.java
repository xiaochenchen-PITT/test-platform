package com.cxc.test.platform.web.datacheck.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceLocatorVO {

    private Long id;

    private Long configId;

    private String targetTableName;

    private String locateField;

    private String locateMethodName;

    private String locateMethodArgs;
}
