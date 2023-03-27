package com.cxc.test.platform.datacheck.domain.config;

import com.cxc.test.platform.datacheck.domain.mapping.SourceMappingItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class DataCheckConfig {

    // 是否异步执行（线程池）
    @Getter
    @Setter
    private Boolean runAsync = true;

    // 部分特殊的字段需要提前校验，和提前结束
    private List<String> CHECK_ADVANCE_FIELDS = new ArrayList<>();

    /**
     * 要对比的字段列表配置
     * 若为空，则全量校验DataConfig中的mappingRuleList
     * 若不为空，则只校验这个配置map中的表+字段
     */
    @Setter
    private Map<String, Set<String>> tableAndCheckFieldsMap;

    public static DataCheckConfig newInstance(Boolean runAsync) {
        DataCheckConfig dataCheckConfig = new DataCheckConfig();
        dataCheckConfig.CHECK_ADVANCE_FIELDS = new ArrayList<>();
        dataCheckConfig.tableAndCheckFieldsMap = new HashMap<>();
        dataCheckConfig.setRunAsync(runAsync);
        return dataCheckConfig;
    }

    public Map<String, Set<String>> getTableAndCheckFieldsMap() {
        if (tableAndCheckFieldsMap == null) {
            tableAndCheckFieldsMap = new HashMap<>();
        }

        return tableAndCheckFieldsMap;
    }

    public boolean checkInAdvance(SourceMappingItem sourceMappingItem) {
        // 和source table无关的target value需要提前校验
        if (sourceMappingItem == null || StringUtils.isEmpty(sourceMappingItem.getTableName())) {
            return true;
        }

        return sourceMappingItem.getFieldNameList().stream().anyMatch(name -> CHECK_ADVANCE_FIELDS.contains(name));
    }
}
