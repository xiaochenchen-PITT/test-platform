package com.cxc.test.platform.migration.domain;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CustomizedMethod {

    private String beanName;

    private List<Object> args;

    public List<Object> getArgs() {
        if (CollectionUtils.isEmpty(args)) {
            args = new ArrayList<>();
        }

        return args;
    }
}
