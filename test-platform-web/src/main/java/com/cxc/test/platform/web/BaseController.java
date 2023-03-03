package com.cxc.test.platform.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@CrossOrigin
@Controller
public class BaseController {

    @Resource
    HttpServletRequest request;

    @GetMapping("/index")
    public String index(){
        return "index";
    }

    public String buildTriggerUrl() {
        StringBuffer triggerUrlSb = request.getRequestURL();
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
