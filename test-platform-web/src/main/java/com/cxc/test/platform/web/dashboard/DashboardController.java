package com.cxc.test.platform.web.dashboard;

import com.cxc.test.platform.web.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@CrossOrigin
@Controller
@RequestMapping(value = "/dashboard")
public class DashboardController extends BaseController {

    @GetMapping("/test")
    public String test() {
        return "dashboard/test";
    }

    @GetMapping("/budget")
    public String budget() {
        return "dashboard/budget";
    }

    @GetMapping("/it")
    public String it() {
        return "dashboard/it";
    }

    @GetMapping("/automation")
    public String automation() {
        return "dashboard/automation";
    }

}
