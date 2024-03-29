package com.cxc.test.platform.infra.domain.datacheck;

import lombok.Data;

import java.util.Date;

@Data
public class SourceLocatorPO {

    /**
     * 主键id，无实际业务意义
     */
    private Long id;

    private Long configId;

    private String targetTableName;

    private String locateField;

    /**
     * 自定义定位source表主键id的逻辑方法
     */
    private String locateMethodName;

    /**
     * 自定义定位source表主键id的逻辑方法的入参，逗号分割
     */
    private String locateMethodArgs;

    private Date createdTime;

    private Date modifiedTime;
}
