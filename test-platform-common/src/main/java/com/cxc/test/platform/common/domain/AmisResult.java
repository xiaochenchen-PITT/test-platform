package com.cxc.test.platform.common.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;
import java.util.*;

public class AmisResult implements Serializable {

    /**
     * 返回 0，表示当前接口正确返回，否则按错误请求处理
     */
    private Integer status;

    /**
     * json格式，必须返回一个具有 key-value 结构的对象
     */
    private String data;

    /**
     * 错误信息
     */
    private String msg;

    /**
     * 弹框时间，时间是毫秒
     */
    private Long msgTimeout;
    
    private AmisResult(Integer status, String data, String msg, Long msgTimeout) {
        this.status = status;
        this.data = data;
        this.msg = msg;
        this.msgTimeout = msgTimeout;
    }

    public static AmisResult success(String data) {
        return new AmisResult(0, data, null, null);
    }

    public static AmisResult simpleSuccess(String data) {
        JSONObject dataJO = new JSONObject();
        dataJO.put("key", data);

        return new AmisResult(0, dataJO.toJSONString(), null, null);
    }

    public static AmisResult fail(String msg, Long msgTimeout) {
        return new AmisResult(-1, null, msg ,msgTimeout);
    }

    public static AmisResult from(ResultDO resultDO) {
        Integer status = resultDO.getIsSuccess() ? 0 : -1;

        String data = null;
        if (resultDO.getData() == null) {
            data = null;
        } else if (resultDO.getData() instanceof Map) {
            data = JSONObject.toJSONString((Map) resultDO.getData());
        } else if (resultDO.getData() instanceof Collection) {
            List<String> list = new ArrayList<>();
            for (Object o : (Collection) resultDO.getData()) {
                list.add(String.valueOf(o));
            }

            JSONObject dataJO = new JSONObject();
            dataJO.put("count", list.size());
            dataJO.put("rows", list);

            data = dataJO.toJSONString();
        } else if (resultDO.getData() instanceof String) {
            JSONObject dataJO = new JSONObject();
            dataJO.put("key", String.valueOf(resultDO.getData()));
            data = dataJO.toJSONString();
        } else {
            data = JSON.toJSONString(resultDO.getData());
        }

        return new AmisResult(status, data, resultDO.getErrorMessage(), null);
    }

    @Override
    public String toString() {
        return "AmisResult{" +
                "status=" + status +
                ", data=" + data +
                ", msg=" + msg +
                ", msgTimeout=" + msgTimeout +
                '}';
    }

    public static void main(String[] args) {
        JSONObject dataJO = new JSONObject();
        System.out.println(dataJO.toJSONString());
        System.out.println(StringUtils.isNotEmpty(dataJO.toJSONString()));
    }
}
