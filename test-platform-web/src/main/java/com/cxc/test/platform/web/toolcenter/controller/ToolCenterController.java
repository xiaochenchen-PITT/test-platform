package com.cxc.test.platform.web.toolcenter.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxc.test.platform.common.domain.AmisResult;
import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.common.utils.ErrorMessageUtils;
import com.cxc.test.platform.infra.domain.toolcenter.ToolPO;
import com.cxc.test.platform.infra.mapper.master.ToolMapper;
import com.cxc.test.platform.toolcenter.domain.Tool;
import com.cxc.test.platform.toolcenter.domain.ToolParam;
import com.cxc.test.platform.toolcenter.domain.ToolQuery;
import com.cxc.test.platform.toolcenter.domain.ToolStatusConstant;
import com.cxc.test.platform.toolcenter.service.ToolCenterService;
import com.cxc.test.platform.web.BaseController;
import com.cxc.test.platform.web.toolcenter.vo.FullToolVO;
import com.cxc.test.platform.web.toolcenter.vo.ToolVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@CrossOrigin
@Controller
@RequestMapping(value = "/toolcenter")
public class ToolCenterController extends BaseController {

    @Resource
    ToolCenterService toolCenterService;

    @Resource
    ToolMapper toolMapper;

    @GetMapping("/list")
    public String list() {
        return "toolcenter/toolList";
    }

    @GetMapping("/add")
    public String add() {
        return "toolcenter/addTool";
    }

    @GetMapping("/run")
    public String run() {
        return "toolcenter/runTool";
    }

    @RequestMapping(value = "/add_tool", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult addTool(@RequestBody FullToolVO fullToolVO) {
        try {
            String triggerUrl = buildTriggerUrl();

            Long toolId = System.currentTimeMillis();
            Tool tool = Tool.builder()
                .toolId(toolId)
                .name(fullToolVO.getName())
                .desc(fullToolVO.getDesc())
                .type(fullToolVO.getType())
                .beanName(fullToolVO.getBeanName())
                .bean(fullToolVO.getBean())
                .url(fullToolVO.getUrl())
                .status(ToolStatusConstant.ACTIVE)
                .creator(fullToolVO.getCreator())
                .domain(fullToolVO.getDomain())
                .build();

            // 入参
            JSONArray paramComboJA = fullToolVO.getParamCombo();
            List<ToolParam> toolParamList = new ArrayList<>();
            for (Object paramComboO : paramComboJA) {
                JSONObject paramComboJO = new JSONObject((LinkedHashMap) paramComboO);

                ToolParam toolParam = ToolParam.builder()
                    .toolId(toolId)
                    .name(paramComboJO.getString("name"))
                    .desc(paramComboJO.getString("desc"))
                    .paramClass(paramComboJO.getString("paramClass"))
                    .isRequired(paramComboJO.getBooleanValue("isRequired"))
                    .hasDefault(paramComboJO.getBooleanValue("hasDefault"))
                    .defaultValue(paramComboJO.getString("defaultValue"))
                    .inputType(paramComboJO.getString("inputType"))
                    .optionValueList(ToolParam.getOptionValueListFromStr(paramComboJO.getString("optionValues")))
                    .build();

                toolParamList.add(toolParam);
            }

            tool.setToolParamList(toolParamList);

            ResultDO<Long> ret = toolCenterService.addTool(tool);
            if (!ret.getIsSuccess()) {
                return AmisResult.fail("新建工具失败，请检查日志", null);
            }

            return AmisResult.simpleSuccess("success", "新建成功，tool id:" + toolId);
        } catch (Exception e) {
            log.error("failed to addTool", e);
            return AmisResult.fail(ErrorMessageUtils.getMessage(e), null);
        }
    }

    @RequestMapping(value = "/get_tool_list", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getToolList(@RequestParam(required = false) String nameSearch, @RequestParam(required = false) String typeSearch,
                                  @RequestParam(required = false) String statusSearch, @RequestParam(required = false) String creatorSearch,
                                  @RequestParam(required = false) String domainSearch) {
        String triggerUrl = buildTriggerUrl();

        ToolQuery toolQuery = ToolQuery.builder()
            .name(nameSearch)
            .type(typeSearch)
            .status(statusSearch)
            .creator(creatorSearch)
            .domain(domainSearch)
            .build();

        ResultDO<List<Tool>> queryRet = toolCenterService.queryTools(toolQuery);
        if (!queryRet.getIsSuccess()) {
            return AmisResult.emptySuccess();
        }

        List<ToolVO> toolVOList = new ArrayList<>();
        for (Tool tool : queryRet.getData()) {
            ToolVO toolVO = ToolVO.builder()
                .toolId(tool.getToolId())
                .name(tool.getName())
                .desc(tool.getDesc())
                .type(tool.getType())
                .bean(tool.isJavaTool() ? tool.getBean() : null)
                .url(tool.isHttpTool() ? tool.getUrl() : null)
                .status(tool.getStatus())
                .creator(tool.getCreator())
                .domain(tool.getDomain())
                .build();

            toolVOList.add(toolVO);
        }

        JSONObject retData = new JSONObject();
        retData.put("rows", toolVOList);
        retData.put("count", toolVOList.size());

        return AmisResult.success(retData, "ok");
    }

    @RequestMapping(value = "/get_domains", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getDomains() {
        String triggerUrl = buildTriggerUrl();

        ToolQuery toolQuery = ToolQuery.builder().build();
        ResultDO<List<Tool>> queryRet = toolCenterService.queryTools(toolQuery);
        if (!queryRet.getIsSuccess()) {
            return AmisResult.emptySuccess();
        }

        List<JSONObject> domains = new ArrayList<>();
        queryRet.getData().stream().forEach(tool -> {
                JSONObject jo = new JSONObject();
                jo.put("label", tool.getDomain());
                jo.put("value", tool.getDomain());

                domains.add(jo);
            }
        );

        JSONObject retData = new JSONObject();
        retData.put("options", domains);

        return AmisResult.success(retData, "ok");
    }

    @RequestMapping(value = "/update_tool", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult updateTool(@RequestParam Long id, @RequestBody ToolVO toolVO) {
        String triggerUrl = buildTriggerUrl();

        ToolPO toolPO = new ToolPO();
        toolPO.setId(id);
        toolPO.setToolId(toolVO.getToolId());
        toolPO.setName(toolVO.getName());
        toolPO.setDesc(toolVO.getDesc());
        toolPO.setType(toolVO.getType());
        toolPO.setBeanName(toolVO.getBeanName());
        toolPO.setBean(toolVO.getBean());
        toolPO.setUrl(toolVO.getUrl());
        toolPO.setStatus(toolVO.getStatus());
        toolPO.setCreator(toolVO.getCreator());
        toolPO.setDomain(toolVO.getDomain());

        int ret = toolMapper.update(toolPO);
        Assert.isTrue(ret == 1, "update tool failed");

        return AmisResult.simpleSuccess("success", "编辑成功");
    }

    @RequestMapping(value = "/delete_tool", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult deleteTool(@RequestParam Long id) {
        String triggerUrl = buildTriggerUrl();

        ToolPO toolPO = new ToolPO();
        toolPO.setId(id);
        toolPO.setStatus(ToolStatusConstant.DELETED);

        int ret = toolMapper.update(toolPO);
        Assert.isTrue(ret == 1, "delete tool failed");

        return AmisResult.simpleSuccess("success", "删除成功");
    }



}
