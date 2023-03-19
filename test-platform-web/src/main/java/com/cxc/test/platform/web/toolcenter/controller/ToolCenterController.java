package com.cxc.test.platform.web.toolcenter.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxc.test.platform.common.domain.AmisResult;
import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.common.utils.ErrorMessageUtils;
import com.cxc.test.platform.infra.domain.toolcenter.ToolPO;
import com.cxc.test.platform.infra.domain.toolcenter.ToolParamPO;
import com.cxc.test.platform.infra.mapper.master.ToolMapper;
import com.cxc.test.platform.infra.mapper.master.ToolParamMapper;
import com.cxc.test.platform.toolcenter.domain.Tool;
import com.cxc.test.platform.toolcenter.domain.ToolParam;
import com.cxc.test.platform.toolcenter.domain.ToolQuery;
import com.cxc.test.platform.toolcenter.domain.ToolStatusConstant;
import com.cxc.test.platform.toolcenter.service.ToolCenterService;
import com.cxc.test.platform.web.BaseController;
import com.cxc.test.platform.web.toolcenter.vo.FullToolVO;
import com.cxc.test.platform.web.toolcenter.vo.ToolParamVO;
import com.cxc.test.platform.web.toolcenter.vo.ToolVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin
@Controller
@RequestMapping(value = "/toolcenter")
public class ToolCenterController extends BaseController {

    @Resource
    ToolCenterService toolCenterService;

    @Resource
    ToolMapper toolMapper;

    @Resource
    ToolParamMapper toolParamMapper;

    @GetMapping("/list")
    public String list() {
        return "toolcenter/toolList";
    }

    @GetMapping("/params")
    public String paramList() {
        return "toolcenter/paramList";
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
            List<ToolParam> toolParamList = new ArrayList<>();

            JSONArray paramComboJA = fullToolVO.getParamCombo();
            if (paramComboJA != null) {
                for (Object paramComboO : paramComboJA) {
                    JSONObject paramComboJO = new JSONObject((LinkedHashMap) paramComboO);

                    ToolParam toolParam = ToolParam.builder()
                        .toolId(toolId)
                        .name(paramComboJO.getString("name"))
                        .label(paramComboJO.getString("label"))
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
    public AmisResult getToolList(@RequestParam(required = false) Long toolIdSearch, @RequestParam(required = false) String nameSearch,
                                  @RequestParam(required = false) String apiSearch, @RequestParam(required = false) String typeSearch,
                                  @RequestParam(required = false) String statusSearch, @RequestParam(required = false) String creatorSearch,
                                  @RequestParam(required = false) String domainSearch) {
        String triggerUrl = buildTriggerUrl();

        ToolQuery toolQuery = ToolQuery.builder()
            .toolId(toolIdSearch)
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
            if (filterApi(apiSearch, tool)) {
                continue;
            }

            ToolVO toolVO = ToolVO.builder()
                .toolId(tool.getToolId())
                .name(tool.getName())
                .desc(tool.getDesc())
                .type(tool.getType())
                .beanName(tool.getBeanName())
                .api(tool.isJavaTool() ? tool.getBean() : tool.getUrl())
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

    private boolean filterApi(String apiSearch, Tool tool) {
        if (StringUtils.isEmpty(apiSearch)) {
            return false;
        }

        if (tool.isJavaTool() && tool.getBean().contains(apiSearch)) {
            return false;
        }

        if (tool.isHttpTool() && tool.getUrl().contains(apiSearch)) {
            return false;
        }

        return true;
    }

    @RequestMapping(value = "/get_tool", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getTool(@RequestParam Long toolId) {
        String triggerUrl = buildTriggerUrl();

        ToolPO toolPOToQuery = new ToolPO();
        toolPOToQuery.setToolId(toolId);

        List<ToolPO> toolPOList = toolMapper.selectByCondition(toolPOToQuery);
        if (CollectionUtils.isEmpty(toolPOList)) {
            return AmisResult.emptySuccess();
        }

        ToolPO toolPO = toolPOList.get(0);

        ToolVO toolVO = ToolVO.builder()
            .toolId(toolPO.getToolId())
            .desc(toolPO.getDesc())
            .api("java".equalsIgnoreCase(toolPO.getType()) ? toolPO.getBean() : toolPO.getUrl())
            .build();

        return AmisResult.success((JSONObject) JSONObject.toJSON(toolVO), "ok");
    }

    @RequestMapping(value = "/get_param_list", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getParamList(@RequestParam Long toolId, @RequestParam(required = false) String nameSearch) {
        String triggerUrl = buildTriggerUrl();

        ToolQuery toolQuery = ToolQuery.builder().toolId(toolId).build();
        ResultDO<List<Tool>> queryRet = toolCenterService.queryTools(toolQuery);
        if (!queryRet.getIsSuccess() || CollectionUtils.isEmpty(queryRet.getData())) {
            return AmisResult.emptySuccess();
        }

        Tool tool = queryRet.getData().get(0);

        List<ToolParamVO> toolParamVOList = new ArrayList<>();
        for (ToolParam toolParam : tool.getToolParamList()) {
            if (StringUtils.isNotEmpty(nameSearch) && !nameSearch.equalsIgnoreCase(toolParam.getName())) {
                continue;
            }

            ToolParamVO toolParamVO = ToolParamVO.builder()
                .paramId(toolParam.getParamId())
                .toolId(toolParam.getToolId())
                .name(toolParam.getName())
                .label(toolParam.getLabel())
                .desc(toolParam.getDesc())
                .paramClass(toolParam.getParamClass())
                .isRequired(toolParam.isRequired())
                .hasDefault(toolParam.isHasDefault())
                .defaultValue(toolParam.getDefaultValue())
                .inputType(toolParam.getInputType())
                .optionValues(toolParam.getOptionValueListAsStr())
                .build();

            toolParamVOList.add(toolParamVO);
        }

        JSONObject retData = new JSONObject();
        retData.put("rows", toolParamVOList);
        retData.put("count", toolParamVOList.size());

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

        Set<JSONObject> domains = new HashSet<>();
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
    public AmisResult updateTool(@RequestParam Long toolId, @RequestBody ToolVO toolVO) {
        String triggerUrl = buildTriggerUrl();

        ToolPO toolPO = new ToolPO();
        toolPO.setToolId(toolId);
        toolPO.setName(toolVO.getName());
        toolPO.setDesc(toolVO.getDesc());
        toolPO.setType(toolVO.getType());
        toolPO.setBeanName(toolVO.getBeanName());
        toolPO.setBean("java".equalsIgnoreCase(toolVO.getType()) ? toolVO.getApi() : null);
        toolPO.setUrl("http".equalsIgnoreCase(toolVO.getType()) ? toolVO.getApi() : null);
        toolPO.setStatus(toolVO.getStatus());
        toolPO.setCreator(toolVO.getCreator());
        toolPO.setDomain(toolVO.getDomain());

        int ret = toolMapper.update(toolPO);
        Assert.isTrue(ret == 1, "update tool failed");

        return AmisResult.simpleSuccess("success", "编辑成功");
    }

    @RequestMapping(value = "/delete_tool", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult deleteTool(@RequestParam Long toolId) {
        String triggerUrl = buildTriggerUrl();

        ToolPO toolPO = new ToolPO();
        toolPO.setToolId(toolId);
        toolPO.setStatus(ToolStatusConstant.DELETED);

        int ret = toolMapper.update(toolPO);
        Assert.isTrue(ret == 1, "delete tool failed");

        return AmisResult.simpleSuccess("success", "删除成功");
    }

    @RequestMapping(value = "/restore_tool", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult restoreTool(@RequestParam Long toolId) {
        String triggerUrl = buildTriggerUrl();

        ToolPO toolPO = new ToolPO();
        toolPO.setToolId(toolId);
        toolPO.setStatus(ToolStatusConstant.ACTIVE);

        int ret = toolMapper.update(toolPO);
        Assert.isTrue(ret == 1, "restore tool failed");

        return AmisResult.simpleSuccess("success", "恢复成功");
    }

    @RequestMapping(value = "/update_param", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult updateParam(@RequestParam Long toolId, @RequestParam Long paramId, @RequestBody ToolParamVO toolParamVO) {
        String triggerUrl = buildTriggerUrl();

        ToolParamPO toolParamPO = new ToolParamPO();
        toolParamPO.setParamId(paramId);
        toolParamPO.setToolId(toolId);
        toolParamPO.setName(toolParamVO.getName());
        toolParamPO.setLabel(toolParamVO.getLabel());
        toolParamPO.setDesc(toolParamVO.getDesc());
        toolParamPO.setParamClass(toolParamVO.getParamClass());
        toolParamPO.setIsRequired(toolParamVO.getIsRequired() ? 1 : 0);
        toolParamPO.setHasDefault(toolParamVO.getHasDefault() ? 1 : 0);
        toolParamPO.setDefaultValue(toolParamVO.getDefaultValue());
        toolParamPO.setInputType(toolParamVO.getInputType());
        toolParamPO.setOptionValues(toolParamVO.getOptionValues());

        int ret = toolParamMapper.update(toolParamPO);
        Assert.isTrue(ret == 1, "update tool param failed");

        return AmisResult.simpleSuccess("success", "编辑成功");
    }

    @RequestMapping(value = "/delete_param", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult deleteParam(@RequestParam Long toolId, @RequestParam Long paramId) {
        String triggerUrl = buildTriggerUrl();

        int ret = toolParamMapper.delete(paramId);
        Assert.isTrue(ret == 1, "delete tool param failed");

        return AmisResult.simpleSuccess("success", "删除成功");
    }

    @RequestMapping(value = "/get_hot_n", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getHotN(@RequestParam Long n) {
        String triggerUrl = buildTriggerUrl();

        ToolQuery toolQuery = ToolQuery.builder().build();
        ResultDO<List<Tool>> queryRet = toolCenterService.queryTools(toolQuery);
        if (!queryRet.getIsSuccess()) {
            return AmisResult.emptySuccess();
        }

        List<Tool> sortedToolSubList = queryRet.getData().stream()
            .sorted(Comparator.comparingLong(Tool::getSuccessCount).reversed())
            .limit(n)
            .collect(Collectors.toList());

        List<ToolVO> toolVOList = new ArrayList<>();
        int rank = 1;
        for (Tool tool : sortedToolSubList) {
            ToolVO toolVO = ToolVO.builder()
                .toolId(tool.getToolId())
                .name(tool.getName())
                .desc(tool.getDesc())
                .type(tool.getType())
                .beanName(tool.getBeanName())
                .api(tool.isJavaTool() ? tool.getBean() : tool.getUrl())
                .status(tool.getStatus())
                .creator(tool.getCreator())
                .domain(tool.getDomain())
                .totalCount(tool.getTotalCount())
                .successCount(tool.getSuccessCount())
                .rank(String.valueOf(rank))
                .build();

            toolVOList.add(toolVO);
            rank += 1;
        }

        JSONObject retData = new JSONObject();
        retData.put("items", toolVOList);

        return AmisResult.success(retData, "ok");

    }

    @RequestMapping(value = "/auto_schema", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getAutoSchema(@RequestParam Long toolId) {
        String triggerUrl = buildTriggerUrl();

        ToolQuery toolQuery = ToolQuery.builder().toolId(toolId).build();
        ResultDO<List<Tool>> queryRet = toolCenterService.queryTools(toolQuery);
        if (!queryRet.getIsSuccess() || CollectionUtils.isEmpty(queryRet.getData())) {
            return AmisResult.emptySuccess();
        }

        Tool tool = queryRet.getData().get(0);
        List<ToolParam> toolParamList = tool.getToolParamList();

        List<JSONObject> controlList = new ArrayList<>();
        for (ToolParam toolParam : toolParamList) {
            JSONObject paramJO = new JSONObject();
            if (toolParam.isInputType()) {
                paramJO.put("type", "text");
            } else {
                paramJO.put("type", "select");
                List<JSONObject> optionList = new ArrayList<>();
                for (Pair pair : toolParam.getOptionValueList()) {
                    JSONObject optionJO = new JSONObject();
                    optionJO.put("label", pair.getKey());
                    optionJO.put("value", pair.getValue());

                    optionList.add(optionJO);
                }
                paramJO.put("options", optionList);
            }

            paramJO.put("name", toolParam.getName());
            paramJO.put("label", toolParam.getLabel() + "（" + toolParam.getParamClass() + "）");
            paramJO.put("value", toolParam.getDefaultValue());
            paramJO.put("required", toolParam.isRequired());

            controlList.add(paramJO);
        }

        JSONObject retData = new JSONObject();
        retData.put("controls", controlList);

        return AmisResult.success(retData, "ok");
    }

    @RequestMapping(value = "/get_tree_select", method = RequestMethod.GET)
    @ResponseBody
    public AmisResult getTreeSelect() {
        String triggerUrl = buildTriggerUrl();

        ToolQuery toolQuery = ToolQuery.builder().build();
        ResultDO<List<Tool>> queryRet = toolCenterService.queryTools(toolQuery);
        if (!queryRet.getIsSuccess()) {
            return AmisResult.emptySuccess();
        }

        Map<String, List<JSONObject>> optionMap = new HashMap<>();
        for (Tool tool : queryRet.getData()) {
            List<JSONObject> toolJOList = null;
            if (!optionMap.containsKey(tool.getDomain())) {
                toolJOList = new ArrayList<>();
            } else {
                toolJOList = optionMap.get(tool.getDomain());
            }

            JSONObject toolJO = new JSONObject();
            toolJO.put("label", tool.getName());
            toolJO.put("value", tool.getToolId());

            toolJOList.add(toolJO);
            optionMap.put(tool.getDomain(), toolJOList);
        }

        List<JSONObject> options = new ArrayList<>();
        for (Map.Entry<String, List<JSONObject>> entry : optionMap.entrySet()) {
            JSONObject optionJO = new JSONObject();
            optionJO.put("label", entry.getKey());
            optionJO.put("value", entry.getKey());
            optionJO.put("children", entry.getValue());

            options.add(optionJO);
        }

        JSONObject retData = new JSONObject();
        retData.put("options", options);

        return AmisResult.success(retData, "ok");
    }

    @RequestMapping(value = "/trigger", method = RequestMethod.POST)
    @ResponseBody
    public AmisResult trigger(@RequestParam Long toolId, @RequestBody LinkedHashMap<String, Object> paramMap) {
        String triggerUrl = buildTriggerUrl();

        // amis会自动将表单提交之后的data数据填充到数据域中，所以这里要过滤一下除了schema以外的字段
        AmisResult schemaRet = getAutoSchema(toolId);
        List<String> schemaParams = schemaRet.getData().getJSONArray("controls").stream()
            .map(o -> ((JSONObject) o).getString("name"))
            .collect(Collectors.toList());

        LinkedHashMap<String, Object> filteredParamMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            if (schemaParams.contains(entry.getKey())) {
                filteredParamMap.put(entry.getKey(), entry.getValue());
            }
        }

        ToolQuery toolQuery = ToolQuery.builder().toolId(toolId).build();
        ResultDO<List<Tool>> queryRet = toolCenterService.queryTools(toolQuery);
        if (!queryRet.getIsSuccess()) {
            return AmisResult.emptySuccess();
        }

        Tool tool = queryRet.getData().get(0);
        ResultDO<String> ret = toolCenterService.triggerTool(tool, filteredParamMap);

        JSONObject dataJO = new JSONObject();
        dataJO.put("isSuccess", ret.getIsSuccess());
        dataJO.put("errorMessage", ret.getErrorMessage());
        dataJO.put("ret", ret.getData());

        return AmisResult.success(dataJO, "调用成功");
    }

}
