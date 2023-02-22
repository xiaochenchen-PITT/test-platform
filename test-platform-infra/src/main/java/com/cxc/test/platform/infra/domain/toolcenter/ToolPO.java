package com.cxc.test.platform.infra.domain.toolcenter;

import lombok.Builder;
import lombok.Data;

import java.sql.Date;

@Data
@Builder
public class ToolPO {

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
     * java类型工具bean的全类名
     * 需要注册成为一个bean
     */
    private String beanClass;

    /**
     * java类型工具的方法名，例如pushOrder
     * http类型工具的方法，例如get/post
     */
    private String method;

    /**
     * http类型工具的url
     */
    private String url;

    /**
     * 工具状态
     */
    private String status;

    private Date createdTime;

    private Date modifiedTime;
}
