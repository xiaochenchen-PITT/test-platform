package com.cxc.test.platform.common.utils;


import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoModelDataListener extends AnalysisEventListener<Map<Integer, String>> {

    private List<Map<Integer, String>> dataList = new ArrayList<>();

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        dataList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        return;
    }

    public List<Map<Integer, String>> getDataList() {
        return dataList;
    }
}
