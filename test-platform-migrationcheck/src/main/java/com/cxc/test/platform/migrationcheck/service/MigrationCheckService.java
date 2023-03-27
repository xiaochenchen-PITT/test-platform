package com.cxc.test.platform.migrationcheck.service;

import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.common.domain.diff.*;
import com.cxc.test.platform.common.utils.CommonUtils;
import com.cxc.test.platform.common.utils.ErrorMessageUtils;
import com.cxc.test.platform.infra.utils.JdbcUtils;
import com.cxc.test.platform.migrationcheck.domain.CustomizedMethod;
import com.cxc.test.platform.migrationcheck.domain.MigrationData;
import com.cxc.test.platform.migrationcheck.domain.SourceLocator;
import com.cxc.test.platform.migrationcheck.domain.config.MigrationCheckConfig;
import com.cxc.test.platform.migrationcheck.domain.config.MigrationConfig;
import com.cxc.test.platform.migrationcheck.domain.mapping.MappingRule;
import com.cxc.test.platform.migrationcheck.domain.mapping.SourceMappingItem;
import com.cxc.test.platform.migrationcheck.ext.fieldCheck.FieldCheckExt;
import com.cxc.test.platform.migrationcheck.ext.skip.SkipCheckHandlerChain;
import com.cxc.test.platform.migrationcheck.ext.sourceLocate.SourceLocateExt;
import com.cxc.test.platform.migrationcheck.utils.MigrationSpringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 对比核心处理类
 */
@Slf4j
@Component
public class MigrationCheckService {

    @Resource
    DiffService diffService;

    @Resource
    SkipCheckHandlerChain skipCheckHandlerChain;

    private DataSource sourceDataSource;
    private DataSource targetDataSource;

    private final ExecutorService executorService = ThreadPoolFactory.getExecutorService();

    private DiffResult diffResult;

    @Getter
    private boolean isRunning = false;
    private AtomicLong count = new AtomicLong(0);
    private AtomicLong failedCount = new AtomicLong(0);

    class DiffTask implements Callable<DiffDetail> {

        private final Long batchId;
        private final Long configId;
        private final MigrationCheckConfig migrationCheckConfig;
        private final MigrationConfig migrationConfig;
        private final MappingRule mappingRule;
        private final String primaryKey;
        private final MigrationData sourceData;

        public DiffTask(Long batchId, Long configId, MigrationConfig migrationConfig, MigrationCheckConfig migrationCheckConfig,
                        MappingRule mappingRule, String primaryKey, MigrationData sourceData) {
            this.batchId = batchId;
            this.configId = configId;
            this.migrationConfig = migrationConfig;
            this.migrationCheckConfig = migrationCheckConfig;
            this.mappingRule = mappingRule;
            this.primaryKey = primaryKey;
            this.sourceData = sourceData;
        }

        @Override
        public DiffDetail call() throws Exception {
            if (!isRunning) {
                return null;
            }

            String sourceTableName = mappingRule.getSourceMappingItem().getTableName();
            List<String> sourceFieldNameList = mappingRule.getSourceMappingItem().getFieldNameList();

            LinkedHashMap<String, Object> sourceSqlAndValueMap = new LinkedHashMap<>();
            String targetSql = null;
            Object targetValue = null;
            try {
                if (CollectionUtils.isNotEmpty(sourceFieldNameList) && sourceFieldNameList.size() == 1) {
                    try {
                        SkipCheckHandlerChain skipCheckHandlerChain = MigrationSpringUtils.getSkipCheckHandlerChain();
                        /**
                         * skip的shouldSkip只判断source第一个字段
                         * 因为如果涉及到多个source字段组合的话，肯定不会跳过校验了
                         */
                        if (skipCheckHandlerChain.shouldSkip(sourceTableName, sourceFieldNameList.get(0), sourceData, migrationCheckConfig)) {
                            return null;
                        }
                    } catch (Exception e) {
                        log.error("Did not find skipCheckHandlerChain bean", e);
                    }
                }

                // source
                if (StringUtils.isNotEmpty(sourceTableName) && CollectionUtils.isNotEmpty(sourceFieldNameList)) {
                    for (String sourceFieldName : sourceFieldNameList) {
                        String sourceSql = String.format("select %s from %s where %s = \"%s\"",
                                sourceFieldName, sourceTableName, primaryKey, sourceData.getValue(primaryKey));
                        Object sourceValue = sourceData.getValue(sourceFieldName);
                        sourceSqlAndValueMap.put(sourceSql, sourceValue);
                    }
                }

                // target
                String targetTableName = mappingRule.getTargetMappingItem().getTableName();
                String targetFieldName = mappingRule.getTargetMappingItem().getFieldName();

                targetSql = buildTargetSql(targetTableName, targetFieldName, sourceData, primaryKey, migrationConfig);
                targetValue = JdbcUtils.getSingleValueBySql(targetDataSource, targetSql, targetFieldName);

                // 对比
                boolean checkInAdvance = migrationCheckConfig.checkInAdvance(mappingRule.getSourceMappingItem());
                DiffDetail diffDetail = diff(batchId, configId, mappingRule, checkInAdvance, sourceSqlAndValueMap, targetValue, targetSql);

                count.incrementAndGet();
                if (diffDetail != null) {
                    failedCount.incrementAndGet();
                }

                return diffDetail;
            } catch (Exception e) {
                String errMsg = String.format("Failed to execute compare for sourceData: %s, sourceTableName: %s, sourceSqlAndValueMap: %s",
                        sourceData, sourceTableName, sourceSqlAndValueMap);
                log.error(errMsg, e);

                DiffDetail diffDetail = buildDiffDetail(batchId, configId, mappingRule, new ArrayList<>(sourceSqlAndValueMap.values()),
                        null, new ArrayList<>(sourceSqlAndValueMap.keySet()), targetValue, targetSql,
                        DiffTypeConstant.ERROR, ErrorMessageUtils.getMessage(e));
                return diffDetail;
            }
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
                                        MigrationCheckConfig migrationCheckConfig, String triggerUrl, String runningIp) {
        if (isRunning) {
            return ResultDO.fail("当前机器已有任务在运行，请等待其结束之后再触发");
        }

        // 修改java parallelStream的并发量
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "50");

        diffResult = init(batchId, configId, triggerUrl, runningIp);
        try {
            sourceDataSource = JdbcUtils.intiDataSource(migrationConfig.getSourceDbConfig());
            targetDataSource = JdbcUtils.intiDataSource(migrationConfig.getTargetDbConfig());

            Map<String, String> sourceTableAndPrimaryKeyMap = getPrimaryKeyMap(migrationConfig);
            List<MigrationData> sourceDataList = initSourceDataList(migrationConfig, sourceDataSource);
            skipCheckHandlerChain.register();

            List<DiffTask> diffTaskList = new ArrayList<>();
            Set<Object> sourceIdSet = new HashSet<>();
            sourceDataList.parallelStream().forEach(sourceData -> {
                if (skipCheckHandlerChain.shouldSkip(sourceData.getTableName(), null, sourceData, migrationCheckConfig)) {
                    return;
                }

                String sourceTableName = sourceData.getTableName();
                String primaryKey = sourceTableAndPrimaryKeyMap.get(sourceTableName);

                boolean isCovered = false; // 是否已覆盖过没有source表的mapping rule，避免重复添加task
                synchronized (new Object()) {
                    Object sourceIdObj = sourceData.getValue(primaryKey);
                    isCovered = sourceIdSet.contains(sourceIdObj);

                    sourceIdSet.add(sourceIdObj);
                }

                List<MappingRule> validMappingRuleList = migrationConfig.getValidMappingRuleList(sourceTableName, isCovered);
                validMappingRuleList.forEach(mappingRule -> {
                    DiffTask diffTask = new DiffTask(batchId, configId, migrationConfig, migrationCheckConfig, mappingRule, primaryKey, sourceData);
                    diffTaskList.add(diffTask);
//                    log.info("adding one....");
                });
                return;
            });

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

    private DiffResult init(Long batchId, Long configId, String triggerUrl, String runningIp) {
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
        diffResult.setRunningIp(runningIp);

        return diffResult;
    }

    private DiffResult end(DiffResult diffResult) {
        isRunning = false;

        if (diffResult.getTotalCount() == null) {
            diffResult.setTotalCount(count.get());
        }

        diffResult.setProgress(CommonUtils.getPrettyPercentage(count.get(), diffResult.getTotalCount()));

        diffResult.setFailedCount(failedCount.get());

        count.set(0);
        failedCount.set(0);

        return diffResult;
    }

    private List<MigrationData> initSourceDataList(MigrationConfig migrationConfig, DataSource sourceDataSource) {
        List<MigrationData> sourceDataList = new ArrayList<>();
        Map<String, String> tableAndInitSqlMap = migrationConfig.getTableAndInitSqlMap();
        // 遍历每一张表
        for (Map.Entry<String, String> entry : tableAndInitSqlMap.entrySet()) {
            String tableName = entry.getKey();
            String initSourceDataSql = entry.getValue();

            log.info("Initializing full data for source table: {}...", tableName);
            List<Map<String, Object>> sourceDataResultList = JdbcUtils.queryResult(sourceDataSource, initSourceDataSql);
            if (CollectionUtils.isEmpty(sourceDataResultList)) {
                continue;
            }

            // 遍历每一个值
            for (Map<String, Object> sourceDataResult : sourceDataResultList) {
                MigrationData sourceData = new MigrationData();
                sourceData.setTableName(tableName);
                // 遍历每一个字段
                for (Map.Entry<String, Object> fieldAndValueEntry : sourceDataResult.entrySet()) {
                    sourceData.addOneField(fieldAndValueEntry.getKey(), fieldAndValueEntry.getValue());
                }

                sourceDataList.add(sourceData);
            }
        }

        return sourceDataList;
    }

    private DiffDetail diff(Long batchId, Long configId, MappingRule mappingRule, boolean checkInAdvance, LinkedHashMap<String, Object> sourceSqlAndValueMap,
                            Object targetValue, String targetSql) {
        List<String> sourceSqlList = new ArrayList<>();
        List<Object> sourceValueList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : sourceSqlAndValueMap.entrySet()) {
            log.info(String.format("source sql: %s, source value: %s; target sql: %s, target value: %s",
                entry.getKey(), entry.getValue(), targetSql, targetValue));

            sourceSqlList.add(entry.getKey());
            sourceValueList.add(entry.getValue());
        }

        // 部分特殊的字段需要提前校验，提前结束，不去判断ONLY_IN_TARGET和ONLY_IN_SOURCE
        if (CollectionUtils.isNotEmpty(sourceValueList) && sourceValueList.size() == 1) {
            Object sourceValue = sourceValueList.get(0);
            String sourceSql = sourceSqlList.get(0);

            if (checkInAdvance) {
                FieldCheckResult fieldCheckResult = executeFieldCheck(sourceValueList, targetValue, mappingRule);
                if (!fieldCheckResult.isPass()) {
                    String diffType = DiffTypeConstant.VALUE_CHECK_FAIL;
                    DiffDetail detail = buildDiffDetail(batchId, configId, mappingRule, sourceValueList, fieldCheckResult.getComputedValue(),
                        sourceSqlList, targetValue, targetSql, diffType, null);
                    return detail;
                }

                return null;
            }

            // 对某些场景的null值处理
            if (CommonUtils.sameStringInPool(String.valueOf(sourceValue), String.valueOf(targetValue))) {
                return null;
            }

            String diffType = null;
            // 两边部分存在
            if (sourceValue == null && targetValue != null) {
                diffType = DiffTypeConstant.ONLY_IN_TARGET;
            } else if (sourceValue != null && targetValue == null) {
                diffType = DiffTypeConstant.ONLY_IN_SOURCE;
            }

            if (diffType != null) {
                DiffDetail detail = buildDiffDetail(batchId, configId, mappingRule, sourceValueList, sourceValue, sourceSqlList,
                    targetValue, targetSql, diffType, null);
                return detail;
            }
        }

        // 都存在，检查一致性
        FieldCheckResult fieldCheckResult = executeFieldCheck(sourceValueList, targetValue, mappingRule);
        if (!fieldCheckResult.isPass()) {
            String diffType = DiffTypeConstant.VALUE_CHECK_FAIL;
            DiffDetail detail = buildDiffDetail(batchId, configId, mappingRule, sourceValueList, fieldCheckResult.getComputedValue(),
                sourceSqlList, targetValue, targetSql, diffType, null);
            return detail;
        }

        return null;
    }

    private DiffDetail buildDiffDetail(Long batchId, Long configId, MappingRule mappingRule, List<Object> sourceValueList, Object computedSourceValue,
                                       List<String> sourceSqlList, Object targetValue, String targetSql, String diffType, String errorMessage) {
        DiffDetail diffDetail = DiffDetail.builder()
            .batchId(batchId)
            .configId(configId)
            .diffType(diffType)
            .sourceQuery(CollectionUtils.isNotEmpty(sourceSqlList) ? String.valueOf(sourceSqlList) : null)
            .sourceTableName(mappingRule.getSourceMappingItem() != null && StringUtils.isNotEmpty(mappingRule.getSourceMappingItem().getTableName()) ?
                mappingRule.getSourceMappingItem().getTableName() : null)
            .sourceFieldName(mappingRule.getSourceMappingItem() != null && CollectionUtils.isNotEmpty(mappingRule.getSourceMappingItem().getFieldNameList()) ?
                String.valueOf(mappingRule.getSourceMappingItem().getFieldNameList()) : null)
            .sourceValue(CollectionUtils.isNotEmpty(sourceValueList) ? String.valueOf(sourceValueList) : null)
            .computedSourceValue(computedSourceValue != null ? String.valueOf(computedSourceValue) : null)
            .targetQuery(targetSql)
            .targetTableName(mappingRule.getTargetMappingItem().getTableName())
            .targetFieldName(mappingRule.getTargetMappingItem().getFieldName())
            .targetValue(targetValue != null ? String.valueOf(targetValue) : null)
            .errorMessage(errorMessage)
            .build();

        return diffDetail;
    }

    private String buildTargetSql(String targetTable, String targetField, MigrationData sourceData, String sourcePrimaryKey,
                                  MigrationConfig migrationConfig) {
        String sourceId = String.valueOf(sourceData.getValue(sourcePrimaryKey));

        Map<String, SourceLocator> tableAndLocatorMap = migrationConfig.getTableAndLocatorMap();
        SourceLocator locator = tableAndLocatorMap.get(targetTable);
        String locateField = locator.getLocateField();

        CustomizedMethod customizedMethod = locator.getLocateMethod();
        // 没有自定义，有单独字段承载
        if (customizedMethod == null || StringUtils.isEmpty(customizedMethod.getBeanName())) {
            return String.format("select %s from %s where %s = \"%s\"", targetField, targetTable, locateField, sourceId);
        }

        String locateBeanName = customizedMethod.getBeanName();
        SourceLocateExt locateBean = MigrationSpringUtils.getLocateBean(locateBeanName);
        if (locateBean == null) {
            return String.format("select %s from %s where %s = \"%s\"", targetField, targetTable, locateField, sourceId);
        }

        String whereClause = locateBean.locateSource(locateField, sourceData, sourceId, customizedMethod.getArgs());
        return String.format("select %s from %s where %s", targetField, targetTable, whereClause);
    }

    private FieldCheckResult executeFieldCheck(List<Object> sourceValueList, Object targetValue, MappingRule mappingRule) {
        // 没有自定义转换逻辑，平迁对比
        if (mappingRule.getFieldCheckMethod() == null || StringUtils.isEmpty(mappingRule.getFieldCheckMethod().getBeanName())) {
            Assert.isTrue(CollectionUtils.isNotEmpty(sourceValueList) && sourceValueList.size() == 1,
                "source fields have more than 1 field when fieldCheckMethod is empty");

            Object sourceValue = sourceValueList.get(0);
            return FieldCheckResult.builder()
                .isPass(CommonUtils.generalEquals(sourceValue, targetValue))
                .computedValue(sourceValue)
                .build();
        }

        String fcBeanName = mappingRule.getFieldCheckMethod().getBeanName();
        FieldCheckExt fcBean = MigrationSpringUtils.getFcBean(fcBeanName);
        if (fcBean == null) {
            Assert.isTrue(CollectionUtils.isNotEmpty(sourceValueList) && sourceValueList.size() == 1,
                "source fields have more than 1 field when fieldCheckMethod is empty");

            Object sourceValue = sourceValueList.get(0);
            return FieldCheckResult.builder()
                .isPass(CommonUtils.generalEquals(sourceValue, targetValue))
                .computedValue(sourceValue)
                .build();
        }

        // 有自定义转换逻辑
        FieldCheckResult fcResult = fcBean.check(sourceValueList, targetValue, mappingRule.getFieldCheckMethod().getArgs());
        return fcResult;
    }

    private Map<String, String> getPrimaryKeyMap(MigrationConfig migrationConfig) {
        Map<String, String> sourceTableAndPrimaryKeyMap = new HashMap<>();

        for (MappingRule mappingRule : migrationConfig.getMappingRuleList()) {
            SourceMappingItem sourceMappingItem = mappingRule.getSourceMappingItem();

            if (sourceMappingItem == null || StringUtils.isEmpty(sourceMappingItem.getTableName()) ||
                CollectionUtils.isEmpty(sourceMappingItem.getFieldNameList()) || sourceMappingItem.getFieldNameList().size() != 1) {
                continue;
            }

            if (mappingRule.getSourceMappingItem().isPrimaryKey()) {
                sourceTableAndPrimaryKeyMap.put(sourceMappingItem.getTableName(), sourceMappingItem.getFieldNameList().get(0));
            }
        }

        Assert.isTrue(sourceTableAndPrimaryKeyMap.size() == migrationConfig.getRelatedSourceTables().size(),
            "部分源表没有配置主键(primary key)，请检查配置");

        return sourceTableAndPrimaryKeyMap;
    }
}
