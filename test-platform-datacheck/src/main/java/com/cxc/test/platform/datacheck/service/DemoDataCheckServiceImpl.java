package com.cxc.test.platform.datacheck.service;

import com.cxc.test.platform.common.diff.GeneralDiffCheckFacade;
import com.cxc.test.platform.common.diff.GeneralDiffTask;
import com.cxc.test.platform.common.diff.ThreadPoolFactory;
import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.common.domain.diff.*;
import com.cxc.test.platform.common.utils.CommonUtils;
import com.cxc.test.platform.common.utils.ErrorMessageUtils;
import com.cxc.test.platform.datacheck.domain.config.DataCheckConfig;
import com.cxc.test.platform.datacheck.domain.config.DataConfig;
import com.cxc.test.platform.datacheck.ext.skip.SkipCheckHandlerChain;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * copy from DataCheckService
 * 一个简单例子，模拟了一个简单随机任务
 */
@Slf4j
@Component
public class DemoDataCheckServiceImpl implements GeneralDiffCheckFacade {

    @Resource
    DiffService diffService;

    @Resource
    SkipCheckHandlerChain skipCheckHandlerChain;

    private Map<Long, BatchRunningBundle> batchIdAndBundleMap = new HashMap<>();

    private final ExecutorService executorService = ThreadPoolFactory.getDataCheckExecutorService();

    @AllArgsConstructor
    class DiffTask extends GeneralDiffTask {

        private final Long batchId;
        private final Long configId;

        @Override
        public DiffDetail call() throws Exception {
            // debug
            if (!isRunning(batchId)) {
                return null;
            }
            Thread.sleep(1000);

            DiffDetail diffDetail = DiffDetail.builder()
                .batchId(batchId)
                .configId(configId)
                .diffType(RandomUtils.nextBoolean() ? DiffTypeConstant.VALUE_CHECK_FAIL : DiffTypeConstant.ERROR)
                .sourceQuery(RandomStringUtils.random(200, "qwertyuiopasd fghjklzxcvbnm,;'/@$*"))
                .sourceTableName("2")
                .sourceFieldName("3")
                .sourceValue(RandomStringUtils.random(200, "qwertyuiopasd fghjklzxcvbnm,;'/@$*"))
                .computedSourceValue(RandomStringUtils.random(200, "qwertyuiopasd fghjklzxcvbnm,;'/@$*"))
                .targetQuery(RandomStringUtils.random(200, "qwertyuiopasd fghjklzxcvbnm,;'/@$*"))
                .targetTableName("5")
                .targetFieldName("6")
                .targetValue(RandomStringUtils.random(200, "qwertyuiopasd fghjklzxcvbnm,;'/@$*"))
                .errorMessage(RandomStringUtils.random(200, "qwertyuiopasd fghjklzxcvbnm,;'/@$*"))
                .build();

            DiffDetail ret = RandomUtils.nextBoolean() ? diffDetail : null;
            batchIdAndBundleMap.get(batchId).getCount().incrementAndGet();
            if (diffDetail != null) {
                batchIdAndBundleMap.get(batchId).getFailedCount().incrementAndGet();
            }
            return ret;
        }
    }

    @Override
    public DiffResult init(Long batchId, Long configId, String triggerUrl, String runningIp) {
        if (MapUtils.isEmpty(batchIdAndBundleMap)) {
            batchIdAndBundleMap = new HashMap<>();
        }

        batchIdAndBundleMap.put(batchId, new BatchRunningBundle());
        batchIdAndBundleMap.get(batchId).setBatchId(batchId);
        batchIdAndBundleMap.get(batchId).setRunning(false);
        batchIdAndBundleMap.get(batchId).setCount(new AtomicLong(0));
        batchIdAndBundleMap.get(batchId).setFailedCount(new AtomicLong(0));

        DiffResult diffResult = DiffResult.builder().build();
        diffResult.setBatchId(batchId);
        diffResult.setConfigId(configId);
        diffResult.setIsSuccess(true);
        diffResult.setStatus(TaskStatusEnum.RUNNING.getStatus());
        diffResult.setProgress("1%");
        diffResult.setTriggerUrl(triggerUrl);
        diffResult.setRunningIp(runningIp);

        batchIdAndBundleMap.get(batchId).setDiffResult(diffResult);
        return diffResult;
    }

    @Override
    public DiffResult end(DiffResult diffResult) {
        Long batchId = diffResult.getBatchId();

        if (diffResult.getTotalCount() == null) {
            diffResult.setTotalCount(batchIdAndBundleMap.get(batchId).getCount().get());
        }

        diffResult.setFailedCount(batchIdAndBundleMap.get(batchId).getFailedCount().get());

        diffResult.setProgress(CommonUtils.getPrettyPercentage(batchIdAndBundleMap.get(batchId).getCount().get(),
            diffResult.getTotalCount()));

        batchIdAndBundleMap.remove(batchId);

        return diffResult;
    }

    @Override
    public boolean isRunning(Long batchId) {
        return batchIdAndBundleMap.get(batchId) != null && batchIdAndBundleMap.get(batchId).isRunning();
    }

    @Override
    public ResultDO<DiffResult> run(Long batchId, Long configId, String triggerUrl, String runningIp, Map<String, Object> configMap) {
        DiffResult diffResult = init(batchId, configId, triggerUrl, runningIp);

        DataConfig dataConfig = (DataConfig) configMap.get("dataConfig");
        DataCheckConfig dataCheckConfig = (DataCheckConfig) configMap.get("dataCheckConfig");

        // 修改java parallelStream的并发量
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "50");
        try {
            List<DiffTask> diffTaskList = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                DiffTask diffTask = new DiffTask(batchId, configId);
                diffTaskList.add(diffTask);
            }

            boolean removeIf = diffTaskList.removeIf(diffTask -> Objects.isNull(diffTask));
            diffResult.setTotalCount((long) diffTaskList.size());

            // 先初始化t_compare_diff_result
            boolean isRunning = diffService.initDiffResult(diffResult, dataConfig.getValidMappingRuleList().size());
            batchIdAndBundleMap.get(batchId).setRunning(isRunning);

            // 执行对比
            if (dataCheckConfig.getRunAsync()) {
                List<Future<DiffDetail>> futures = executorService.invokeAll(diffTaskList);
                for (Future<DiffDetail> future : futures) {
                    DiffDetail diffDetail = future.get();
                    if (diffDetail != null) {
                        diffResult.getDiffDetailList().add(diffDetail);
                        diffResult.setIsEqual(false);
                    }
                }
            } else {
                for (DemoDataCheckServiceImpl.DiffTask diffTask : diffTaskList) {
                    DiffDetail diffDetail = diffTask.call();
                    if (diffDetail != null) {
                        diffResult.getDiffDetailList().add(diffDetail);
                        diffResult.setIsEqual(false);
                    }
                }
            }
            diffResult.setFailedCount((long) diffResult.getDiffDetailList().size());
            diffResult.setStatus(TaskStatusEnum.FINISHED.getStatus());
        } catch (Exception e) {
            log.error("Failed to execute compare because " + ErrorMessageUtils.getMessage(e), e);
            diffResult.setIsSuccess(false);
            diffResult.setStatus(TaskStatusEnum.FAILED.getStatus());
            diffResult.setErrorMessage(ErrorMessageUtils.getMessage(e));
        } finally {
            diffResult = end(diffResult);

            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
                String.valueOf(Runtime.getRuntime().availableProcessors()));

            boolean saveRet = diffService.saveFinalDiffResult(diffResult);
            log.info("Data check finished, save result: {}, batch id: {}", saveRet, batchId);
        }

        return ResultDO.success(diffResult);
    }

    @Override
    public boolean stop(Long batchId) {
        if (batchIdAndBundleMap.get(batchId) != null) {
            batchIdAndBundleMap.get(batchId).setRunning(false);
        }

        return true;
    }

    // todo 如果是分布式的，需要配合实现分布式锁。或者让定时调度任务只在一台机器上执行
    @Override
    @Scheduled(initialDelay = 10000, fixedRate = 10000)
    public void updateDiffResultOnTime() {
        try {
            ExecutorService timerExecutorService = ThreadPoolFactory.getDataCheckTimerExecutorService();

            timerExecutorService.submit(() -> {
                for (Map.Entry<Long, BatchRunningBundle> entry : batchIdAndBundleMap.entrySet()) {
                    if (entry.getValue().isRunning()) {
                        DiffResult diffResult = entry.getValue().getDiffResult();
                        diffResult.setStatus(TaskStatusEnum.RUNNING.getStatus());
                        diffResult.setProgress(CommonUtils.getPrettyPercentage(entry.getValue().getCount().get(), diffResult.getTotalCount()));
                        diffResult.setFailedCount(entry.getValue().getFailedCount().get());

                        diffService.updateDiffResultOnTime(diffResult);
                    }
                }
            });
        } catch (Exception e) {
            log.error("failed to update DiffResult at regular time", e);
        }
    }
}
