package com.cxc.test.platform.web.apicheck.vo;

import lombok.Data;

import java.util.List;

@Data
public class ApiCheckDbCombo {

    private String checkTableName;

    private List<CheckFieldVO> checkFieldCombo;

    private List<WhiteListFieldVO> dbCheckWhiteListFieldCombo;
}
