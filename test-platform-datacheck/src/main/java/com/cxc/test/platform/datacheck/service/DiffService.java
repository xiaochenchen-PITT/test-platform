package com.cxc.test.platform.datacheck.service;

import com.cxc.test.platform.common.diff.ThreadPoolFactory;
import com.cxc.test.platform.common.domain.FeatureKeyConstant;
import com.cxc.test.platform.common.domain.diff.DiffDetail;
import com.cxc.test.platform.common.domain.diff.DiffResult;
import com.cxc.test.platform.infra.converter.DiffConverter;
import com.cxc.test.platform.infra.domain.diff.DiffDetailPO;
import com.cxc.test.platform.infra.domain.diff.DiffResultPO;
import com.cxc.test.platform.infra.mapper.master.DiffDetailMapper;
import com.cxc.test.platform.infra.mapper.master.DiffResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
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

    private final ExecutorService executorService = ThreadPoolFactory.getDataCheckExecutorService();

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

    public DiffResult getDiffResult(Long batchId) {
        DiffResultPO diffResultPO = diffResultMapper.getByBatchId(batchId);

        if (diffResultPO == null) {
            return null;
        }

        List<DiffDetailPO> diffDetailPOList = diffDetailMapper.getByBatchId(batchId);

        DiffResult diffResult = diffConverter.convertPO2DO(diffResultPO, diffDetailPOList);
        return diffResult;
    }

    public boolean initDiffResult(DiffResult diffResult, int mappingRuleSize) {
        diffResult.addOrUpdateFeature(FeatureKeyConstant.MAPPING_RULE_COUNT, String.valueOf(mappingRuleSize));

        int ret = diffResultMapper.insert(diffConverter.convertDO2PO(diffResult));
        return ret == 1;
    }

    public boolean updateDiffResultOnTime(DiffResult diffResult) {
        int ret = diffResultMapper.update(diffConverter.convertDO2PO(diffResult));
        return ret == 1;
    }

    public boolean saveFinalDiffResult(DiffResult diffResult) {
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

        int ret;
        DiffResultPO diffResultQuery = diffResultMapper.getByBatchId(diffResult.getBatchId());
        if (diffResultQuery == null) {
            ret = diffResultMapper.insert(diffConverter.convertDO2PO(diffResult));
        } else {
            ret = diffResultMapper.update(diffConverter.convertDO2PO(diffResult));
        }

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
