package com.cxc.test.platform.toolcenter.converter;

import com.cxc.test.platform.infra.domain.toolcenter.ToolPO;
import com.cxc.test.platform.infra.domain.toolcenter.ToolParamPO;
import com.cxc.test.platform.toolcenter.domain.Tool;
import com.cxc.test.platform.toolcenter.domain.ToolParam;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ToolConverter {

    public ToolPO convertDO2PO(Tool tool) {
        ToolPO toolPO = new ToolPO();

        toolPO.setId(tool.getId());
        toolPO.setToolId(tool.getToolId());
        toolPO.setName(tool.getName());
        toolPO.setDesc(tool.getDesc());
        toolPO.setType(tool.getType());
        toolPO.setBeanName(tool.getBeanName());
        toolPO.setBeanClass(tool.getBeanClass());
        toolPO.setMethod(tool.getMethod());
        toolPO.setUrl(tool.getUrl());
        toolPO.setStatus(tool.getStatus());
        toolPO.setCreatedTime(tool.getCreatedTime());
        toolPO.setModifiedTime(tool.getModifiedTime());

        return toolPO;
    }

    public ToolParamPO convertDO2PO(ToolParam toolParam) {
        ToolParamPO toolParamPO = new ToolParamPO();

        toolParamPO.setId(toolParam.getId());
        toolParamPO.setToolId(toolParam.getToolId());
        toolParamPO.setName(toolParam.getName());
        toolParamPO.setDesc(toolParam.getDesc());
        toolParamPO.setParamClass(toolParam.getParamClass());
        toolParamPO.setIsRequired(toolParam.isRequired() ? 1 : 0);
        toolParamPO.setHasDefault(toolParam.isHasDefault() ? 1 : 0);
        toolParamPO.setDefaultValue(toolParam.getDefaultValue());
        toolParamPO.setInputType(toolParam.getInputType());
        toolParamPO.setOptionValues(String.join(",", toolParam.getOptionValueList()));
        toolParamPO.setStatus(toolParam.getStatus());
        toolParamPO.setCreatedTime(toolParam.getCreatedTime());
        toolParamPO.setModifiedTime(toolParam.getModifiedTime());

        return toolParamPO;
    }

    public List<ToolParamPO> convertDO2PO(List<ToolParam> toolParamList) {
        List<ToolParamPO> toolParamPOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(toolParamList)) {
            for (ToolParam toolParam : toolParamList) {
                toolParamPOList.add(convertDO2PO(toolParam));
            }
        }

        return toolParamPOList;
    }

    public Tool convertPO2DO(ToolPO toolPO, List<ToolParamPO> toolParamPOList) {
        return Tool.builder()
            .id(toolPO.getId())
            .toolId(toolPO.getToolId())
            .name(toolPO.getName())
            .desc(toolPO.getDesc())
            .type(toolPO.getType())
            .beanName(toolPO.getBeanName())
            .beanClass(toolPO.getBeanClass())
            .method(toolPO.getMethod())
            .url(toolPO.getUrl())
            .status(toolPO.getStatus())
            .toolParamList(convertPO2DO(toolParamPOList))
            .createdTime(toolPO.getCreatedTime())
            .modifiedTime(toolPO.getModifiedTime())
            .build();
    }

    public ToolParam convertPO2DO(ToolParamPO toolParamPO) {
        return ToolParam.builder()
            .id(toolParamPO.getId())
            .toolId(toolParamPO.getToolId())
            .name(toolParamPO.getName())
            .desc(toolParamPO.getDesc())
            .paramClass(toolParamPO.getParamClass())
            .isRequired(toolParamPO.getIsRequired() == 1)
            .hasDefault(toolParamPO.getHasDefault() == 1)
            .defaultValue(toolParamPO.getDefaultValue())
            .inputType(toolParamPO.getInputType())
            .optionValueList(StringUtils.isNotEmpty(toolParamPO.getOptionValues()) ?
                    Arrays.asList(toolParamPO.getOptionValues().split(",")) : null)
            .status(toolParamPO.getStatus())
            .createdTime(toolParamPO.getCreatedTime())
            .modifiedTime(toolParamPO.getModifiedTime())
            .build();
    }

    public List<ToolParam> convertPO2DO(List<ToolParamPO> toolParamPOList) {
        List<ToolParam> toolParamList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(toolParamPOList)) {
            for (ToolParamPO toolParamPO : toolParamPOList) {
                toolParamList.add(convertPO2DO(toolParamPO));
            }
        }

        return toolParamList;
    }
}
