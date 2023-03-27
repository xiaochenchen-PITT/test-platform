package com.cxc.test.platform.datacheck.ext.skip;

import com.cxc.test.platform.datacheck.domain.config.DataCheckConfig;
import com.cxc.test.platform.datacheck.domain.SourceData;
import com.cxc.test.platform.datacheck.ext.skip.impl.CommonSkipHandler;
import com.cxc.test.platform.datacheck.ext.skip.impl.DataCheckConfigSkipHandler;
import com.cxc.test.platform.datacheck.ext.skip.impl.business.oneid.FsupplierTypeSkipHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

// TODO: 2022/10/10 根据业务身份做逻辑隔离，提高效率
@Slf4j
@Component("skipCheckHandlerChain")
public class SkipCheckHandlerChain implements SkipCheckHandler {

    @Resource
    FsupplierTypeSkipHandler fsupplierTypeSkipHandler;

    @Resource
    CommonSkipHandler commonSkipHandler;

    @Resource
    DataCheckConfigSkipHandler dataCheckConfigSkipHandler;

    private List<SkipCheckHandler> handlerList;

    private void addHandler(SkipCheckHandler handler) {
        if (CollectionUtils.isEmpty(handlerList)) {
            handlerList = new ArrayList<>();
        }

        // 避免parallelStream并发导致的重复添加问题
        if (!handlerList.contains(handler)) {
            handlerList.add(handler);
        }
    }

    // init handler chain
    public void register() {
//        addHandler(fsupplierTypeSkipHandler);
//        addHandler(commonSkipHandler);
//        addHandler(dataCheckConfigSkipHandler);
    }

    @Override
    public String getName() {
        return "skipCheckHandlerChain";
    }

    @Override
    public boolean shouldSkip(String sourceTableName, String sourceFieldName, SourceData sourceData, DataCheckConfig dataCheckConfig) {
        try {
            if (CollectionUtils.isEmpty(handlerList)) {
                return false;
            }

            for (SkipCheckHandler handler : handlerList) {
                try {
                    boolean ret = handler.shouldSkip(sourceTableName, sourceFieldName, sourceData, dataCheckConfig);
                    if (ret) {
                        return true;
                    }
                } catch (Exception e) {
                    log.error("Failed to check SkipCheckHandler: " + handler.getName(), e);
                    continue;
                }
            }

            return false;
        } catch (Exception e) {
            log.error("Failed to check all SkipCheckHandler, ", e);
            return false;
        }
    }
}
