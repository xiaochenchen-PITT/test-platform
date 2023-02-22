package com.cxc.test.platform.infra.domain.toolcenter;

import lombok.Builder;
import lombok.Data;

import java.sql.Date;

@Data
@Builder
public class ToolParamPO {

    /**
     * 主键id，即工具参数id
     */
    private Long id;

    /**
     * 关联的工具id
     */
    private Long toolId;

    /**
     * 入参名称
     */
    private String name;

    /**
     * 入参补充描述
     */
    private String desc;

    /**
     * 入参的class类型
     */
    private String paramClass;

    /**
     * 是否必填，1：必填，0：非必填
     */
    private int isRequired;

    /**
     * 是否有默认值，1：有，0：无
     */
    private int hasDefault;

    /**
     * 若有默认值，默认值的值
     */
    private String defaultValue;

    /**
     * 入参输入类型
     * 可选input（输入） / select（单选）
     */
    private String inputType;

    /**
     * 若输入类型为select（单选），可选值的列表，逗号分割
     */
    private String optionValues;

    /**
     * 工具参数状态
     */
    private String status;

    private Date createdTime;

    private Date modifiedTime;
}
