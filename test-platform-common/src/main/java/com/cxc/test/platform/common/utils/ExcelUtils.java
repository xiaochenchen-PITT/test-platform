package com.cxc.test.platform.common.utils;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文档：https://easyexcel.opensource.alibaba.com/
 */
@Slf4j
public class ExcelUtils {

    public static List<Map<Integer, String>> read(String excelFileName) {
        try {
            File directory = new File("");
            String fileAbsoluteName = directory.getCanonicalPath() + File.separator + excelFileName;

            NoModelDataListener listener = new NoModelDataListener();
            EasyExcel.read(fileAbsoluteName, listener).sheet().doRead();

            return listener.getDataList();
        } catch (Exception e) {
            log.error("Failed to handle excel", e);
            return new ArrayList<>();
        }
    }
}
