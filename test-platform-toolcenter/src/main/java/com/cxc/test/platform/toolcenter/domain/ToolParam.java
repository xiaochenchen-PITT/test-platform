package com.cxc.test.platform.toolcenter.domain;

import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ToolParam {

    /**
     * 主键id，无实际业务意义
     */
    private Long id;

    /**
     * 入参id
     */
    private Long paramId;

    /**
     * 关联的工具id
     */
    private Long toolId;

    /**
     * 入参名称
     */
    private String name;

    /**
     * 入参中文名称
     */
    private String label;

    /**
     * 入参补充描述
     */
    private String desc;

    /**
     * 入参的class类型
     */
    private String paramClass;

    /**
     * 是否必填
     */
    private boolean isRequired;

    /**
     * 是否有默认值
     */
    private boolean hasDefault;

    /**
     * 若有默认值，默认值的值
     */
    private String defaultValue;

    /**
     * 入参输入类型
     * 可选input（输入） / select（单选）
     */
    private String inputType;

    /**
     * 若输入类型为select（单选），可选值的列表
     */
    private List<Pair<String, String>> optionValueList;

    private Date createdTime;

    private Date modifiedTime;

    public static List<Pair<String, String>> getOptionValueListFromStr(String str) {
        if (StringUtils.isBlank(str)) {
            return new ArrayList<>();
        }

        JSONObject strJO = JSONObject.parseObject(str);
        List<Pair<String, String>> pairList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : strJO.entrySet()) {
            pairList.add(Pair.of(entry.getKey(), String.valueOf(entry.getValue())));
        }

        return pairList;
    }

    public String getOptionValueListAsStr() {
        if (CollectionUtils.isEmpty(optionValueList)) {
            return null;
        }

        JSONObject jo = new JSONObject(true);
        for (Pair<String, String> pair : optionValueList) {
            jo.put(pair.getKey(), pair.getValue());
        }

        return jo.toJSONString();
    }

    public boolean isInputType() {
        return "input".equalsIgnoreCase(inputType);
    }

    public boolean isSelectType() {
        return "select".equalsIgnoreCase(inputType);
    }
}
