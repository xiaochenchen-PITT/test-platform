package com.cxc.test.platform.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@CrossOrigin
@RestController
public class BaseController {

    @Resource
    HttpServletRequest request;

    public String buildTriggerUrl() {
        StringBuffer triggerUrlSb = request.getRequestURL();
        triggerUrlSb.append("?");
        boolean first = true;
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if (first) {
                triggerUrlSb.append("?");
            } else {
                triggerUrlSb.append("&");
            }

            triggerUrlSb.append(entry.getKey()).append("=").append(Arrays.toString(entry.getValue()));
            first = false;
        }

        String triggerUrl = triggerUrlSb.toString();
        log.info("trigger url: {}", triggerUrl);
        return triggerUrl;
    }

}
