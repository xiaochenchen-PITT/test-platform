package com.cxc.test.platform.web.toolcenter;

import com.cxc.test.platform.web.BaseController;
import com.cxc.test.platform.common.domain.AmisResult;
import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.toolcenter.domain.Tool;
import com.cxc.test.platform.toolcenter.domain.ToolQuery;
import com.cxc.test.platform.toolcenter.service.ToolCenterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@CrossOrigin
@Controller
@RequestMapping(value = "/toolcenter")
public class ToolCenterController extends BaseController {

    @Resource
    ToolCenterService toolCenterService;

    @GetMapping("/test_list")
    public String testList(){
        return "toolcenter/list";
    }

    // /testCbgCrm?env=test&config=table1@f1,f2;table2@f3,f4
    @RequestMapping(value = "/addTool", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult testDemo(@RequestParam(value = "env") String env, @RequestParam(value = "config", required = false) String config,
                               @RequestParam(value = "limit", required = false) Integer limit) {
        String triggerUrl = buildTriggerUrl();

//        toolCenterService.addTool()
        return AmisResult.simpleSuccess("success", null);
    }

    @RequestMapping(value = "/querytool", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult queryToolList() {
        ToolQuery toolQuery = ToolQuery.builder()
            .build();

        ResultDO<List<Tool>> ret = toolCenterService.queryTools(toolQuery);
        AmisResult amisResult = AmisResult.from(ret);

        return amisResult;
    }
}
