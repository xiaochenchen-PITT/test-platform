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
        return ToolPO.builder()
            .id(tool.getId())
            .toolId(tool.getToolId())
            .name(tool.getName())
            .desc(tool.getDesc())
            .type(tool.getType())
            .beanName(tool.getBeanName())
            .beanClass(tool.getBeanClass())
            .method(tool.getMethod())
            .url(tool.getUrl())
            .status(tool.getStatus())
            .createdTime(tool.getCreatedTime())
            .modifiedTime(tool.getModifiedTime())
            .build();
    }

    public ToolParamPO convertDO2PO(ToolParam toolParam) {
        return ToolParamPO.builder()
            .id(toolParam.getId())
            .toolId(toolParam.getToolId())
            .name(toolParam.getName())
            .desc(toolParam.getDesc())
            .paramClass(toolParam.getParamClass())
            .isRequired(toolParam.isRequired() ? 1 : 0)
            .hasDefault(toolParam.isHasDefault() ? 1 : 0)
            .defaultValue(toolParam.getDefaultValue())
            .inputType(toolParam.getInputType())
            .optionValues(String.join(",", toolParam.getOptionValueList()))
            .status(toolParam.getStatus())
            .createdTime(toolParam.getCreatedTime())
            .modifiedTime(toolParam.getModifiedTime())
            .build();
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
