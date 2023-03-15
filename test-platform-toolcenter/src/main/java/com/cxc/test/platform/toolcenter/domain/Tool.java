package com.cxc.test.platform.toolcenter.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Tool {

    public static final String BEAN_SPLITER = "#";
    public static final String URL_SPLITER = ":";

    /**
     * 主键id，无实际业务意义
     */
    private Long id;

    /**
     * 工具id
     */
    private Long toolId;

    /**
     * 工具名称
     */
    private String name;

    /**
     * 工具补充描述
     */
    private String desc;

    /**
     * 工具类型，可选java/http
     * feign类型调用需要自行封装为本地java方法
     */
    private String type;

    /**
     * java类型工具bean的名称
     * 需要注册成为一个bean
     */
    private String beanName;

    /**
     * java类型工具bean方法
     * 需要注册成为一个bean
     */
    private String bean;

    /**
     * http类型工具的url
     */
    private String url;

    /**
     * 工具状态
     */
    private String status;

    /**
     * 工具的入参方法列表
     */
    private List<ToolParam> toolParamList;

    /**
     * 工具创建人
     */
    private String creator;

    /**
     * 工具所属领域
     */
    private String domain;

    /**
     * 总调用数
     */
    private Long totalCount;

    /**
     * 成功调用数
     */
    private Long successCount;

    private Date createdTime;

    private Date modifiedTime;

    public Long getSuccessCount() {
        if (successCount == null) {
            return 0L;
        }

        return successCount;
    }

    public Long getTotalCount() {
        if (totalCount == null) {
            return 0L;
        }

        return totalCount;
    }

    public List<ToolParam> getToolParamList() {
        if (toolParamList == null) {
            toolParamList = new ArrayList<>();
        }

        return toolParamList;
    }

    public boolean isJavaTool() {
        return "java".equalsIgnoreCase(type);
    }

    public boolean isHttpTool() {
        return "http".equalsIgnoreCase(type);
    }

}
