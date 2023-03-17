package com.cxc.test.platform.migrationcheck.service;

import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.common.domain.diff.*;
import com.cxc.test.platform.common.utils.CommonUtils;
import com.cxc.test.platform.common.utils.ErrorMessageUtils;
import com.cxc.test.platform.migrationcheck.domain.config.MigrationCheckConfig;
import com.cxc.test.platform.migrationcheck.domain.config.MigrationConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * copy from MigrationCheckService
 * 一个简单例子，模拟了一个简单随机任务
 */
@Slf4j
@Component
public class DemoMigrationCheckService {

    @Resource
    DiffService diffService;

    private final ExecutorService executorService = ThreadPoolFactory.getExecutorService();

    private DiffResult diffResult;

    @Getter
    private boolean isRunning = false;
    private AtomicLong count = new AtomicLong(0);
    private AtomicLong failedCount = new AtomicLong(0);

    class DiffTask implements Callable<DiffDetail> {

        private final Long batchId;
        private final Long configId;

        public DiffTask(Long batchId, Long configId) {
            this.batchId = batchId;
            this.configId = configId;
        }

        @Override
        public DiffDetail call() throws Exception {
            // debug
            if (!isRunning) {
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
            count.incrementAndGet();
            if (ret != null) {
                failedCount.incrementAndGet();
            }

            return ret;
        }
    }

    // todo 如果是分布式的，需要配合实现分布式锁。或者让定时调度任务只在一台机器上执行
    @Scheduled(initialDelay = 10000, fixedRate = 10000)
    public void updateDiffResultOnTime() {
        try {
            if (isRunning) {
                diffResult.setStatus(TaskStatusEnum.RUNNING.getStatus());
                diffResult.setProgress(CommonUtils.getPrettyPercentage(count.get(), diffResult.getTotalCount()));
                diffResult.setFailedCount(failedCount.get());

                diffService.updateDiffResultOnTime(diffResult);
            }
        } catch (Exception e) {
            log.error("failed to update DiffResult at regular time", e);
        }
    }

    public boolean stop() {
        isRunning = false;
        return true;
    }

    /**
     * 对比服务入口
     *
     * @param batchId              批次id，每次不同
     * @param configId             配置id
     * @param migrationConfig      字段对应关系
     * @param migrationCheckConfig 迁移校验的配置
     * @return
     */
    public ResultDO<DiffResult> compare(Long batchId, Long configId, MigrationConfig migrationConfig,
                                        MigrationCheckConfig migrationCheckConfig, String triggerUrl) {
        if (isRunning) {
            return ResultDO.fail("当前机器已有任务在运行，请等待其结束之后再触发");
        }

        // 修改java parallelStream的并发量
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "50");

        diffResult = init(batchId, configId, triggerUrl);
        try {
            List<DiffTask> diffTaskList = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                DiffTask diffTask = new DiffTask(batchId, configId);
                diffTaskList.add(diffTask);
            }

            boolean removeIf = diffTaskList.removeIf(diffTask -> Objects.isNull(diffTask));
            diffResult.setTotalCount((long) diffTaskList.size());

            // 先初始化t_compare_diff_result
            isRunning = diffService.initDiffResult(diffResult, migrationConfig.getValidMappingRuleList().size());

            // 执行对比
            if (migrationCheckConfig.getRunAsync()) {
                List<Future<DiffDetail>> futures = executorService.invokeAll(diffTaskList);
                for (Future<DiffDetail> future : futures) {
                    DiffDetail diffDetail = future.get();
                    if (diffDetail != null) {
                        diffResult.getDiffDetailList().add(diffDetail);
                        diffResult.setIsEqual(false);
                    }
                }
            } else {
                for (DiffTask diffTask : diffTaskList) {
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
            end(diffResult);

            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
                String.valueOf(Runtime.getRuntime().availableProcessors()));

            boolean saveRet = diffService.saveFinalDiffResult(diffResult);
            log.info("Migration check finished, save result: {}, batch id: {}", saveRet, batchId);
        }

        return ResultDO.success(diffResult);
    }

    private DiffResult init(Long batchId, Long configId, String triggerUrl) {
        isRunning = false;
        count.set(0);
        failedCount.set(0);

        DiffResult diffResult = DiffResult.builder().build();
        diffResult.setBatchId(batchId);
        diffResult.setConfigId(configId);
        diffResult.setIsSuccess(true);
        diffResult.setStatus(TaskStatusEnum.RUNNING.getStatus());
        diffResult.setProgress("1%");
        diffResult.setTriggerUrl(triggerUrl);

        return diffResult;
    }

    private DiffResult end(DiffResult diffResult) {
        isRunning = false;

        diffResult.setProgress(CommonUtils.getPrettyPercentage(count.get(), diffResult.getTotalCount()));
        diffResult.setFailedCount(failedCount.get());

        count.set(0);
        failedCount.set(0);

        return diffResult;
    }

}
