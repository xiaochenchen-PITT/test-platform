package com.cxc.test.platform.migration.service;

import com.cxc.test.platform.common.domain.diff.DiffDetail;
import com.cxc.test.platform.common.domain.diff.DiffResult;
import com.cxc.test.platform.infra.converter.DiffConverter;
import com.cxc.test.platform.infra.mapper.xytest.DiffDetailMapper;
import com.cxc.test.platform.infra.mapper.xytest.DiffResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
@Component
public class DiffService {

    @Resource
    DiffResultMapper diffResultMapper;

    @Resource
    DiffDetailMapper diffDetailMapper;

    @Resource
    DiffConverter diffConverter;

    private final ExecutorService executorService = ThreadPoolFactory.getExecutorService();

    class DbOperateTask implements Callable<Integer> {

        private final DiffDetail diffDetail;

        public DbOperateTask(DiffDetail diffDetail) {
            this.diffDetail = diffDetail;
        }

        @Override
        public Integer call() throws Exception {
            int ret = diffDetailMapper.insert(diffConverter.convertDO2PO(diffDetail));
            return ret;
        }
    }

    public boolean saveDiffResult(DiffResult diffResult) {
        boolean saveRet = true;

        // 默认值
        if (diffResult.getIsSuccess() == null) {
            diffResult.setIsSuccess(true);
        }
        if (diffResult.getIsEqual() == null) {
            diffResult.setIsEqual(CollectionUtils.isEmpty(diffResult.getDiffDetailList()));
        }
        if (diffResult.getCreatedTime() == null) {
            diffResult.setCreatedTime(new Date(System.currentTimeMillis()));
        }
        if (diffResult.getModifiedTime() == null) {
            diffResult.setModifiedTime(new Date(System.currentTimeMillis()));
        }

        int ret = diffResultMapper.insert(diffConverter.convertDO2PO(diffResult));
        if (ret != 1) {
            saveRet = false;
        }

        List<DbOperateTask> dbOperateTaskList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(diffResult.getDiffDetailList())) {
            for (DiffDetail diffDetail : diffResult.getDiffDetailList()) {
                dbOperateTaskList.add(new DbOperateTask(diffDetail));
            }

            try {
                List<Future<Integer>> futures = executorService.invokeAll(dbOperateTaskList);
                for (Future<Integer> future : futures) {
                    if (future.get() != 1) {
                        saveRet = false;
                    }
                }
            } catch (Exception e) {
                log.error("Failed to operate db in parallel", e);
                saveRet = false;
            }
        }

        return saveRet;
    }
}
