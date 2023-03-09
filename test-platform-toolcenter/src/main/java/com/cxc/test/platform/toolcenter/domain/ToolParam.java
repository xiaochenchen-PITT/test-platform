package com.cxc.test.platform.toolcenter.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class ToolParam {

    /**
     * 主键id，即参数id
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
     * 是否必填
     */
    private boolean isRequired;

    /**
     * 是否有默认值
     */
    private boolean hasDefault;

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
     * 若输入类型为select（单选），可选值的列表
     */
    private List<String> optionValueList;

    /**
     * 工具参数状态
     */
    private String status;

    private Date createdTime;

    private Date modifiedTime;
}
