package com.cxc.test.platform.common.domain;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Data
public class AmisResult implements Serializable {

    private static final String DEFAULT_SUCCESS_MSG = "保存成功";

    /**
     * 返回 0，表示当前接口正确返回，否则按错误请求处理
     */
    private Integer status;

    /**
     * json格式，必须返回一个具有 key-value 结构的对象
     */
    private JSONObject data;

    /**
     * 错误信息
     */
    private String msg;

    /**
     * 弹框时间，时间是毫秒
     */
    private Long msgTimeout;
    
    private AmisResult(Integer status, JSONObject data, String msg, Long msgTimeout) {
        this.status = status;
        this.data = data;
        this.msg = msg;
        this.msgTimeout = msgTimeout;
    }

    public static AmisResult emptySuccess() {
        JSONObject data = new JSONObject();
        data.put("rows", new ArrayList<>());
        return new AmisResult(0, data, null, null);
    }

    public static AmisResult success(JSONObject data, String msg) {
        return new AmisResult(0, data, msg, null);
    }

    public static AmisResult simpleSuccess(String data, String msg) {
        JSONObject dataJO = new JSONObject();
        dataJO.put("key", data);

        String retMsg = StringUtils.isEmpty(msg) ? DEFAULT_SUCCESS_MSG : msg;
        return new AmisResult(0, dataJO, retMsg, null);
    }

    public static AmisResult fail(String msg, Long msgTimeout) {
        return new AmisResult(-1, null, msg ,msgTimeout);
    }

    public static AmisResult from(ResultDO resultDO) {
        Integer status = resultDO.getIsSuccess() ? 0 : -1;

        JSONObject data = null;
        if (resultDO.getData() == null) {
            data = null;
        } else if (resultDO.getData() instanceof Map) { // map
            data = (JSONObject) resultDO.getData();
        } else if (resultDO.getData() instanceof Collection) { // collections
            List<String> list = new ArrayList<>();
            for (Object o : (Collection) resultDO.getData()) {
                list.add(String.valueOf(o));
            }

            data.put("count", list.size());
            data.put("rows", list);
        } else if (resultDO.getData() instanceof String) { // String
            data.put("key", String.valueOf(resultDO.getData()));
        } else { // 对象
            data = (JSONObject) resultDO.getData();
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
}
