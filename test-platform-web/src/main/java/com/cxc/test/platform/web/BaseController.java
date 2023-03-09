package com.cxc.test.platform.web;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxc.test.platform.common.domain.AmisResult;
import com.cxc.test.platform.common.utils.ErrorMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
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

    @RequestMapping(value = "/get_menu", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getMenu() {
        try {
            File file = ResourceUtils.getFile("classpath:menu.json");
            String menuJson = FileUtils.readFileToString(file, "UTF-8");

            JSONArray menuJA = JSONArray.parseArray(menuJson);
            JSONObject dataJO = new JSONObject();
            dataJO.put("nav", menuJA);

            return AmisResult.success(dataJO, "ok");
        } catch (Exception e){
            log.error("failed to get/parse menu json file, please check.", e);
            return AmisResult.fail(ErrorMessageUtils.getMessage(e), null);
        }
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
