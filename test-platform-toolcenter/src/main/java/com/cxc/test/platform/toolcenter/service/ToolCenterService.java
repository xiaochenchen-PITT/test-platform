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
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public ResultDO<Long> addTool(Tool tool) {
        try {
            Long toolId = tool.getToolId();
            int ret = toolMapper.insert(toolConverter.convertDO2PO(tool));
            Assert.isTrue(ret == 1, "failed to add tool");

            if (CollectionUtils.isEmpty(tool.getToolParamList())) {
                return ResultDO.success(toolId);
            }

            for (ToolParam toolParam : tool.getToolParamList()) {
                toolParam.setToolId(toolId);
                toolParam.setParamId(System.currentTimeMillis());
                int paramRet = toolParamMapper.insert(toolConverter.convertDO2PO(toolParam));
                Assert.isTrue(paramRet == 1, "failed to add tool param");
                Thread.sleep(1);
            }

            return ResultDO.success(toolId);
        } catch (Exception e) {
            log.error("addTool failed. ", e);
//            return ResultDO.fail(ErrorMessageUtils.getMessage(e)); // 回滚必须要抛异常，这里不return了
            throw new RuntimeException("addTool failed, please check log"); // 回滚默认需要抛RuntimeException或者Error类
        }
    }

    public ResultDO<Boolean> updateTool(Tool tool) {
        try {
            int ret = toolMapper.update(toolConverter.convertDO2PO(tool));

            Assert.isTrue(ret == 1, "failed to update tool");
            return ResultDO.success(Boolean.TRUE);
        } catch (Exception e) {
            log.error("updateTool failed. ", e);
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

            int ret = toolMapper.update(toolConverter.convertDO2PO(tool));
            Assert.isTrue(ret == 1, "failed to delete tool");
            return ResultDO.success(Boolean.TRUE);
        } catch (Exception e) {
            log.error("deleteTool failed. ", e);
            return ResultDO.fail(ErrorMessageUtils.getMessage(e));
        }
    }

    public ResultDO<Boolean> updateToolParam(ToolParam toolParam) {
        try {
            int ret = toolParamMapper.update(toolConverter.convertDO2PO(toolParam));
            Assert.isTrue(ret == 1, "failed to update tool param");

            return ResultDO.success(Boolean.TRUE);
        } catch (Exception e) {
            log.error("updateToolParam failed. ", e);
            return ResultDO.fail(ErrorMessageUtils.getMessage(e));
        }
    }

    public ResultDO<Boolean> updateToolParams(List<ToolParam> toolParamList) {
        try {
            if (CollectionUtils.isEmpty(toolParamList)) {
                return ResultDO.success(Boolean.TRUE);
            }

            for (ToolParam toolParam : toolParamList) {
                int ret = toolParamMapper.update(toolConverter.convertDO2PO(toolParam));
                Assert.isTrue(ret == 1, "failed to update tool param");
            }

            return ResultDO.success(Boolean.TRUE);
        } catch (Exception e) {
            log.error("updateToolParams failed. ", e);
            return ResultDO.fail(ErrorMessageUtils.getMessage(e));
        }
    }

    public ResultDO<Boolean> deleteToolParam(Long paramId) {
        try {
            ResultDO<List<ToolParam>> queryResult = queryToolParams(ToolParamQuery.builder().paramId(paramId).build());
            if (!queryResult.getIsSuccess()) {
                return ResultDO.success(Boolean.TRUE);
            }

            int ret = toolParamMapper.delete(paramId);
            Assert.isTrue(ret == 1, "failed to delete tool param");
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

            if (toolQuery.getToolId() != null && toolQuery.getToolId() > 0) {
                toolPOToQuery.setToolId(toolQuery.getToolId());
            }
            if (StringUtils.isNotEmpty(toolQuery.getName())) {
                toolPOToQuery.setName(toolQuery.getName());
            }
            if (StringUtils.isNotEmpty(toolQuery.getType())) {
                toolPOToQuery.setType(toolQuery.getType());
            }
            if (StringUtils.isEmpty(toolQuery.getStatus())) {
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

            Assert.isTrue(toolParamQuery.getToolId() != null && toolParamQuery.getToolId() > 0,
                "invalid toolParamQuery, toolId should be greater than 0");
            Long toolId = toolParamQuery.getToolId();

            ToolPO toolPOQuery = new ToolPO();
            toolPOQuery.setToolId(toolId);
            ToolPO toolPO = toolMapper.selectByCondition(toolPOQuery).get(0);
            Tool tool = toolConverter.convertPO2DO(toolPO, null);

            // 查询
            toolParamPOToQuery.setToolId(toolId);
            if (toolParamQuery.getParamId() != null && toolParamQuery.getParamId() > 0) {
                toolParamPOToQuery.setId(toolParamQuery.getParamId());
            }
            if (StringUtils.isNotEmpty(toolParamQuery.getToolParamName())) {
                toolParamPOToQuery.setName(toolParamQuery.getToolParamName());
            }

            List<ToolParamPO> toolParamPOList = toolParamMapper.selectByCondition(toolParamPOToQuery);

            // java接口类工具特殊逻辑，需要提前把入参带出来
            if (CollectionUtils.isEmpty(toolParamPOList) && tool.isJavaTool()) {
                String beanClass = tool.getBean().split(Tool.BEAN_SPLITER)[0];
                String method = tool.getBean().split(Tool.BEAN_SPLITER)[1];
                Map<String, String> paramMap = getJavaMethodDetail(beanClass, method);

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
            String beanClass = tool.getBean().split(Tool.BEAN_SPLITER)[0];
            Class<?> toolClass = Class.forName(beanClass);
            Object toolBean = SpringUtils.getBean(tool.getBeanName(), toolClass);

            Class<?>[] parameterTypes = new Class<?>[paramAndValueMap.size()];
            for (int i = 0; i < paramAndValueMap.size(); i++) {
                parameterTypes[i] = Class.forName(tool.getToolParamList().get(i).getParamClass());
            }

            Object[] paramValues = new Class<?>[paramAndValueMap.size()];
            for (int i = 0; i < paramAndValueMap.size(); i++) {
                paramValues[i] = paramAndValueMap.get(tool.getToolParamList().get(i).getName());
            }

            String methodStr = tool.getBean().split(Tool.BEAN_SPLITER)[1];
            Method method = toolClass.getMethod(methodStr, parameterTypes);
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

            String httpMethod = tool.getUrl().split(Tool.URL_SPLITER)[0];
            String httpUrl = tool.getUrl().split(Tool.URL_SPLITER)[1];
            String ret = null;
            if ("get".equalsIgnoreCase(httpMethod)) {
                ret = HttpClient.get(httpUrl, basicNameValuePairs);
            } else if ("post".equalsIgnoreCase(httpMethod)) {
                ret = HttpClient.post(httpUrl, basicNameValuePairs);
            } else {
                String errorMessage = "ToolCenterService.triggerHttp invalid http url: " + tool.getUrl();
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
