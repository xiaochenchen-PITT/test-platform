package com.cxc.test.platform.migrationcheck.domain.locate;

import com.cxc.test.platform.migrationcheck.domain.CustomizedMethod;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceLocator {

    /**
     * 目标表中承载sourceId的的字段
     */
    private String locateField;

    /**
     * 自定义定位source表主键id的逻辑
     * bean需要实现SourceLocateExt的locateSource方法
     */
    private CustomizedMethod locateMethod;
}
