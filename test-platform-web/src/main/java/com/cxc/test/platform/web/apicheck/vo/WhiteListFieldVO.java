package com.cxc.test.platform.web.apicheck.vo;

import lombok.Data;

@Data
public class WhiteListFieldVO {

    // api返回字段校验的白名单
    private String responseJsonPath;

    // 数据库字段校验的白名单
    private String dbField;
}
