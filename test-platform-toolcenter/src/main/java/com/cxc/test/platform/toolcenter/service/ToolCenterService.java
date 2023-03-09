package com.cxc.test.platform.toolcenter.service;

import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.common.utils.ErrorMessageUtils;
import com.cxc.test.platform.common.utils.HttpClient;
import com.cxc.test.platform.common.utils.SpringUtils;
import com.cxc.test.platform.infra.domain.toolcenter.ToolPO;
import com.cxc.test.platform.infra.domain.toolcenter.ToolParamPO;
import com.cxc.test.platform.infra.mapper.master.ToolMapper;
import com.cxc.test.platform.infra.mapper.master.ToolParamMapper;
import com.cxc.test.platform.toolcenter.converter.ToolConverter;
import com.cxc.test.platform.toolcenter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 工具的增删改查和调用
 */
@Slf4j
@Component
public class ToolCenterService {

    @Resource
    ToolMapper toolMapper;

    @Resource
    ToolParamMapper toolParamMapper;

    @Resource
    ToolConverter toolConverter;

    public ResultDO<Long> addTool(Tool tool) {
        try {
            int ret = toolMapper.insert(toolConverter.convertDO2PO(tool));
            Assert.isTrue(ret == 1, "failed to add tool");
            return ResultDO.success(tool.getToolId());
        } catch (Exception e) {
            log.error("addTool failed. ", e);
            return ResultDO.fail(ErrorMessageUtils.getMessage(e));
        }
    }

    public ResultDO<Tool> editTool(Tool tool) {
        try {
            ToolPO newToolPO = toolMapper.update(toolConverter.convertDO2PO(tool));

            ToolParamQuery toolParamQuery = ToolParamQuery.builder()
                .toolId(newToolPO.getToolId())
                .build();
            ResultDO<List<ToolParam>> paramQueryResult = queryToolParams(toolParamQuery);

            Tool newTool = null;
            if (paramQueryResult.getIsSuccess()) {
                newTool = toolConverter.convertPO2DO(newToolPO, toolConverter.convertDO2PO(paramQueryResult.getData()));
            } else {
                newTool = toolConverter.convertPO2DO(newToolPO, null);
            }
            return ResultDO.success(newTool);
        } catch (Exception e) {
            log.error("addTool failed. ", e);
            return ResultDO.fail(ErrorMessageUtils.getMessage(e));
        }
    }

    public ResultDO<Boolean> deleteTool(Long toolId) {
        try {
            ResultDO<List<Tool>> queryResult = queryTools(ToolQuery.builder().toolId(toolId).build());
            if (!queryResult.getIsSuccess()) {
                return ResultDO.success(Boolean.TRUE);
            }

            Tool tool = queryResult.getData().get(0);
            tool.setStatus(ToolStatusConstant.DELETED);

            ToolPO newToolPO = toolMapper.update(toolConverter.convertDO2PO(tool));
            Assert.notNull(newToolPO, "failed to delete tool");
            return ResultDO.success(Boolean.TRUE);
        } catch (Exception e) {
            log.error("deleteTool failed. ", e);
            return ResultDO.fail(ErrorMessageUtils.getMessage(e));
        }
    }

    public ResultDO<Boolean> addParamsForTool(Long toolId, List<ToolParam> toolParamList) {
        try {
            if (CollectionUtils.isEmpty(toolParamList)) {
                return ResultDO.success(Boolean.TRUE);
            }

            for (ToolParam toolParam : toolParamList) {
                toolParam.setToolId(toolId);
                int ret = toolParamMapper.insert(toolConverter.convertDO2PO(toolParam));
                Assert.isTrue(ret == 1, "failed to add tool param");
            }

            return ResultDO.success(Boolean.TRUE);
        } catch (Exception e) {
            log.error("addParamsForTool failed. ", e);
            return ResultDO.fail(ErrorMessageUtils.getMessage(e));
        }
    }

    public ResultDO<Boolean> editToolParam(ToolParam toolParam) {
        try {
            ToolParamPO newToolParamPO = toolParamMapper.update(toolConverter.convertDO2PO(toolParam));
            Assert.notNull(newToolParamPO, "failed to add tool param");

            return ResultDO.success(Boolean.TRUE);
        } catch (Exception e) {
            log.error("editToolParam failed. ", e);
            return ResultDO.fail(ErrorMessageUtils.getMessage(e));
        }
    }

    public ResultDO<Boolean> editToolParams(List<ToolParam> toolParamList) {
        try {
            if (CollectionUtils.isEmpty(toolParamList)) {
                return ResultDO.success(Boolean.TRUE);
            }

            for (ToolParam toolParam : toolParamList) {
                ToolParamPO newToolParamPO = toolParamMapper.update(toolConverter.convertDO2PO(toolParam));
                Assert.notNull(newToolParamPO, "failed to add tool param");
            }

            return ResultDO.success(Boolean.TRUE);
        } catch (Exception e) {
            log.error("editToolParams failed. ", e);
            return ResultDO.fail(ErrorMessageUtils.getMessage(e));
        }
    }

    public ResultDO<Boolean> deleteToolParam(Long paramId) {
        try {
            ResultDO<List<ToolParam>> queryResult = queryToolParams(ToolParamQuery.builder().id(paramId).build());
            if (!queryResult.getIsSuccess()) {
                return ResultDO.success(Boolean.TRUE);
            }

            ToolParam toolParam = queryResult.getData().get(0);
            toolParam.setStatus(ToolStatusConstant.DELETED);

            ToolParamPO newToolParamPO = toolParamMapper.update(toolConverter.convertDO2PO(toolParam));
            Assert.notNull(newToolParamPO, "failed to delete tool param");
            return ResultDO.success(Boolean.TRUE);
        } catch (Exception e) {
            log.error("deleteToolParam failed. ", e);
            return ResultDO.fail(ErrorMessageUtils.getMessage(e));
        }
    }

    public ResultDO<List<Tool>> queryTools(ToolQuery toolQuery) {
        try {
            ToolPO toolPOToQuery = new ToolPO();
            toolPOToQuery.setStatus(ToolStatusConstant.ACTIVE);// 默认查生效的工具

            if (toolQuery.getId() != null && toolQuery.getId() > 0) {
                toolPOToQuery.setId(toolQuery.getId());
            }
            if (toolQuery.getToolId() != null && toolQuery.getToolId() > 0) {
                toolPOToQuery.setToolId(toolQuery.getToolId());
            }
            if (StringUtils.isNotEmpty(toolQuery.getToolName())) {
                toolPOToQuery.setName(toolQuery.getToolName());
            }
            if (StringUtils.isNotEmpty(toolQuery.getToolType())) {
                toolPOToQuery.setType(toolQuery.getToolType());
            }
            if (toolQuery.isQueryDeleted()) {
                toolPOToQuery.setStatus(null);
            }

            List<ToolPO> toolPOList = toolMapper.selectByCondition(toolPOToQuery);
            List<Tool> toolList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(toolPOList)) {
                for (ToolPO toolPO : toolPOList) {
                    ToolParamPO toolParamPOToQuery = new ToolParamPO();
                    toolParamPOToQuery.setToolId(toolPO.getToolId());

                    List<ToolParamPO> toolParamPOList = toolParamMapper.selectByCondition(toolParamPOToQuery);
                    toolList.add(toolConverter.convertPO2DO(toolPO, toolParamPOList));
                }
            }

            return ResultDO.success(toolList);
        } catch (Exception e) {
            log.error("queryTools failed. toolQuery is: " + toolQuery, e);
            return ResultDO.fail(ErrorMessageUtils.getMessage(e));
        }
    }

    public ResultDO<List<ToolParam>> queryToolParams(ToolParamQuery toolParamQuery) {
        try {
            ToolParamPO toolParamPOToQuery = new ToolParamPO();
            toolParamPOToQuery.setStatus(ToolStatusConstant.ACTIVE); // 默认查生效的工具参数

            Assert.isTrue(toolParamQuery.getToolId() != null && toolParamQuery.getToolId() > 0,
                "invalid toolParamQuery, toolId should be greater than 0");
            Long toolId = toolParamQuery.getToolId();

            ToolPO toolPOQuery = new ToolPO();
            toolPOQuery.setToolId(toolId);
            ToolPO toolPO = toolMapper.selectByCondition(toolPOQuery).get(0);
            Tool tool = toolConverter.convertPO2DO(toolPO, null);

            // 查询
            toolParamPOToQuery.setToolId(toolId);
            if (toolParamQuery.getId() != null && toolParamQuery.getId() > 0) {
                toolParamPOToQuery.setId(toolParamQuery.getId());
            }
            if (StringUtils.isNotEmpty(toolParamQuery.getToolParamName())) {
                toolParamPOToQuery.setName(toolParamQuery.getToolParamName());
            }

            List<ToolParamPO> toolParamPOList = toolParamMapper.selectByCondition(toolParamPOToQuery);

            // java接口类工具特殊逻辑，需要提前把入参带出来
            if (CollectionUtils.isEmpty(toolParamPOList) && tool.isJavaTool()) {
                Map<String, String> paramMap = getJavaMethodDetail(tool.getBeanClass(), tool.getMethod());

                List<ToolParam> toolParamList = new ArrayList<>();
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    ToolParam toolParam = ToolParam.builder()
                        .name(entry.getKey())
                        .paramClass(entry.getValue())
                        .build();

                    toolParamList.add(toolParam);
                }

                return ResultDO.success(toolParamList);
            } else {
                return ResultDO.success(toolConverter.convertPO2DO(toolParamPOList));
            }
        } catch (Exception e) {
            log.error("queryToolParams failed. toolParamQuery is: " + toolParamQuery, e);
            return ResultDO.fail(ErrorMessageUtils.getMessage(e));
        }
    }

    /**
     * java方法的获取入参
     *
     * @param serviceName java全类名
     * @param methodName  方法名
     * @return key：入参的名字，入参的类型
     */
    private Map<String, String> getJavaMethodDetail(String serviceName, String methodName) {
        Class<?> beanType;
        try {
            beanType = Class.forName(serviceName);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
            return null;
        }

        if (SpringUtils.getBean(beanType) == null) {
            return null;
        }

        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        Method[] methods;
        if (beanType.getName().contains("CGLIB$$")) {
            methods = beanType.getSuperclass().getDeclaredMethods();
        } else {
            methods = beanType.getDeclaredMethods();
        }

        Method theMethod = null;
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                theMethod = method;
                break;
            }
        }

        if (theMethod == null) {
            return null;
        }

        List<String> paramNames = Arrays.asList(u.getParameterNames(theMethod));
        List<String> paramTypes = Arrays.stream(theMethod.getParameters())
            .map(parameter -> parameter.getType().getTypeName())
            .collect(Collectors.toList());
        Map<String, String> paramMap = new LinkedHashMap<>();
        for (int i = 0; i < paramNames.size(); i++) {
            paramMap.put(paramNames.get(i), paramTypes.get(i));
        }

        return paramMap;
    }

    public ResultDO<String> triggerTool(Tool tool, LinkedHashMap<String, String> paramAndValueMap) {
        if (tool.isJavaTool()) {
            return triggerJava(tool, paramAndValueMap);
        } else if (tool.isHttpTool()) {
            return triggerHttp(tool, paramAndValueMap);
        } else {
            String errorMessage = "triggerTool invalid tool type: " + tool.getType();
            log.error(errorMessage);
            return ResultDO.fail(errorMessage);
        }
    }

    public ResultDO<String> triggerJava(Tool tool, LinkedHashMap<String, String> paramAndValueMap) {
        try {
            Class<?> toolClass = Class.forName(tool.getBeanClass());
            Object toolBean = SpringUtils.getBean(tool.getBeanName(), toolClass);

            Class<?>[] parameterTypes = new Class<?>[paramAndValueMap.size()];
            for (int i = 0; i < paramAndValueMap.size(); i++) {
                parameterTypes[i] = Class.forName(tool.getToolParamList().get(i).getParamClass());
            }

            Object[] paramValues = new Class<?>[paramAndValueMap.size()];
            for (int i = 0; i < paramAndValueMap.size(); i++) {
                paramValues[i] = paramAndValueMap.get(tool.getToolParamList().get(i).getName());
            }

            Method method = toolClass.getMethod(tool.getMethod(), parameterTypes);
            Object ret = method.invoke(toolBean, paramValues);

            return ResultDO.success(String.valueOf(ret));
        } catch (Exception e) {
            log.error("triggerJava failed: " + tool, e);
            return ResultDO.fail(ErrorMessageUtils.getMessage(e));
        }
    }

    public ResultDO<String> triggerHttp(Tool tool, LinkedHashMap<String, String> paramAndValueMap) {
        try {
            List<BasicNameValuePair> basicNameValuePairs = new ArrayList<>();
            paramAndValueMap.forEach((k, v) -> basicNameValuePairs.add(new BasicNameValuePair(k, v)));

            String httpMethod = tool.getMethod();
            String ret = null;
            if ("get".equalsIgnoreCase(httpMethod)) {
                ret = HttpClient.get(tool.getUrl(), basicNameValuePairs);
            } else if ("post".equalsIgnoreCase(httpMethod)) {
                ret = HttpClient.post(tool.getUrl(), basicNameValuePairs);
            } else {
                String errorMessage = "ToolCenterService.triggerHttp invalid http method: " + tool.getMethod();
                log.error(errorMessage);
                return ResultDO.fail(errorMessage);
            }

            return ResultDO.success(ret);
        } catch (Exception e) {
            log.error("triggerHttp failed: " + tool, e);
            return ResultDO.fail(ErrorMessageUtils.getMessage(e));
        }
    }
}
