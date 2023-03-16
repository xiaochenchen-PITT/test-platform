package com.cxc.test.platform.common.utils;

import org.apache.commons.lang3.StringUtils;

public class ErrorMessageUtils {

    public static String getMessage(Throwable t) {
        if (StringUtils.isNotEmpty(t.getMessage())) {
            return t.getMessage();
        }

        if (StringUtils.isNotEmpty(String.valueOf(t.getCause()))) {
            return String.valueOf(t.getCause());
        }

        if (StringUtils.isNotEmpty(String.valueOf(t))) {
            return String.valueOf(t);
        }

        return "General Exception";
    }
}
