package com.cxc.test.platform.migration.service;

import com.cxc.test.platform.common.domain.diff.DiffDetail;
import com.cxc.test.platform.common.domain.diff.DiffResult;
import com.cxc.test.platform.common.domain.diff.DiffTypeConstant;
import com.cxc.test.platform.common.domain.diff.FieldCheckResult;
import com.cxc.test.platform.common.utils.CommonUtils;
import com.cxc.test.platform.common.utils.ErrorMessageUtils;
import com.cxc.test.platform.infra.utils.JdbcUtils;
import com.cxc.test.platform.migration.domain.CustomizedMethod;
import com.cxc.test.platform.migration.domain.config.MigrationCheckConfig;
import com.cxc.test.platform.migration.domain.config.MigrationConfig;
import com.cxc.test.platform.migration.domain.data.MigrationData;
import com.cxc.test.platform.migration.domain.data.MigrationFullData;
import com.cxc.test.platform.migration.domain.locate.SourceLocator;
import com.cxc.test.platform.migration.domain.mapping.MappingRule;
import com.cxc.test.platform.migration.domain.mapping.SourceMappingItem;
import com.cxc.test.platform.migration.domain.mapping.TargetMappingItem;
import com.cxc.test.platform.migration.ext.fieldCheck.FieldCheckExt;
import com.cxc.test.platform.migration.ext.skip.SkipCheckHandlerChain;
import com.cxc.test.platform.migration.ext.sourceLocate.SourceLocateExt;
import com.cxc.test.platform.migration.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

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

    class DiffTask implements Callable<DiffDetail> {

        private final Long batchId;
        private final MigrationCheckConfig migrationCheckConfig;
        private final MigrationConfig migrationConfig;
        private final MappingRule mappingRule;
        private final String primaryKey;
        private final MigrationData sourceData;

        public DiffTask(Long batchId, MigrationConfig migrationConfig, MigrationCheckConfig migrationCheckConfig,
                        MappingRule mappingRule, String primaryKey, MigrationData sourceData) {
            this.batchId = batchId;
            this.migrationConfig = migrationConfig;
            this.migrationCheckConfig = migrationCheckConfig;
            this.mappingRule = mappingRule;
            this.primaryKey = primaryKey;
            this.sourceData = sourceData;
        }

        @Override
        public DiffDetail call() throws Exception {
            String sourceTableName = mappingRule.getSourceMappingItem().getTableName();
            String sourceFieldName = mappingRule.getSourceMappingItem().getFieldName();

            String sourceSql = null;
            Object sourceValue = null;
            String targetSql = null;
            Object targetValue = null;
            try {
                try {
                    SkipCheckHandlerChain skipCheckHandlerChain = SpringUtils.getSkipCheckHandlerChain();
                    if (skipCheckHandlerChain.shouldSkip(sourceTableName, sourceFieldName, sourceData, migrationCheckConfig)) {
                        return null;
                    }
                } catch (Exception e) {
                    log.error("Did not find skipCheckHandlerChain bean", e);
                }

                // source
                sourceSql = String.format("select %s from %s where %s = %s",
                    sourceFieldName, sourceTableName, primaryKey, sourceData.getValue(primaryKey));
                sourceValue = sourceData.getValue(sourceFieldName);

                // target
                String targetTableName = mappingRule.getTargetMappingItem().getTableName();
                String targetFieldName = mappingRule.getTargetMappingItem().getFieldName();

                targetSql = buildTargetSql(targetTableName, targetFieldName, sourceData, primaryKey, sourceTableName, migrationConfig);
                targetValue = JdbcUtils.getSingleValueBySql(targetDataSource, targetSql, targetFieldName);

                // 对比
                boolean checkInAdvance = migrationCheckConfig.checkInAdvance(mappingRule.getSourceMappingItem().getFieldName());
                DiffDetail diffDetail = diff(batchId, mappingRule, checkInAdvance, sourceValue, sourceSql, targetValue, targetSql);
                return diffDetail;
            } catch (Exception e) {
                String errMsg = String.format("Failed to execute compare for source data: %s, source table: %s, source field: %s",
                    sourceData, sourceTableName, sourceFieldName);
                log.error(errMsg, e);

                DiffDetail diffDetail = buildDiffDetail(batchId, mappingRule, sourceValue, sourceSql, targetValue, targetSql,
                    DiffTypeConstant.ERROR, ErrorMessageUtils.getMessage(e));
                return diffDetail;
            }
        }
    }

    /**
     * 对比服务入口
     * @param batchId  批次id
     * @param migrationConfig  字段对应关系
     * @param migrationCheckConfig  迁移校验的配置
     *
     * @return
     */
    public DiffResult compare(Long batchId, MigrationConfig migrationConfig, MigrationCheckConfig migrationCheckConfig, String triggerUrl) {
        // 修改java parallelStream的并发量
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "50");

        DiffResult diffResult = DiffResult.builder().build();
        diffResult.setBatchId(batchId);
        diffResult.setTriggerUrl(triggerUrl);
        diffResult.setIsSuccess(true);

        try {
            sourceDataSource = JdbcUtils.intiDataSource(migrationConfig.getSourceDbConfig());
            targetDataSource = JdbcUtils.intiDataSource(migrationConfig.getTargetDbConfig());

            Map<String, String> sourceTableAndPrimaryKeyMap = getPrimaryKeyMap(migrationConfig);
            MigrationFullData sourceFullData = initSourceFullData(migrationConfig, sourceDataSource);
            skipCheckHandlerChain.register();

            List<DiffTask> diffTaskList = new ArrayList<>();
            sourceFullData.getFullData().parallelStream().forEach(sourceData -> {
                if (skipCheckHandlerChain.shouldSkip(sourceData.getTableName(), null, sourceData, migrationCheckConfig)) {
                    return;
                }

                String sourceTableName = sourceData.getTableName();
                String primaryKey = sourceTableAndPrimaryKeyMap.get(sourceTableName);

                List<MappingRule> validMappingRuleList = migrationConfig.getValidMappingRuleList(sourceTableName);
                validMappingRuleList.forEach(mappingRule -> {
                    DiffTask diffTask = new DiffTask(batchId, migrationConfig, migrationCheckConfig, mappingRule, primaryKey, sourceData);
                    diffTaskList.add(diffTask);
//                    log.info("adding one....");
                });
                return;
            });

            boolean removeIf = diffTaskList.removeIf(diffTask -> Objects.isNull(diffTask));
            diffResult.setTotalCount((long) diffTaskList.size());

            List<Future<DiffDetail>> futures = executorService.invokeAll(diffTaskList);
            for (Future<DiffDetail> future : futures) {
                DiffDetail diffDetail = future.get();
                if (diffDetail != null) {
                    diffResult.getDiffDetailList().add(diffDetail);
                    diffResult.setIsEqual(false);
                }
            }
            diffResult.setFailedCount((long) diffResult.getDiffDetailList().size());
        } catch (Exception e) {
            log.error("Failed to execute compare because " + ErrorMessageUtils.getMessage(e), e);
            diffResult.setIsSuccess(false);
            diffResult.setErrorMessage(ErrorMessageUtils.getMessage(e));
        } finally {
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
                String.valueOf(Runtime.getRuntime().availableProcessors()));

            boolean saveRet = diffService.saveDiffResult(diffResult);
            log.info("Migration check finished, save result: {}, batch id: {}", saveRet, batchId);
        }

        return diffResult;
    }

    private MigrationFullData initSourceFullData(MigrationConfig migrationConfig, DataSource sourceDataSource) {
        MigrationFullData sourceFullData = new MigrationFullData();
        Map<String, String> tableAndSourceInitSqlMap = migrationConfig.getTableAndSourceInitSqlMap();
        // 遍历每一张表
        for (Map.Entry<String, String> entry : tableAndSourceInitSqlMap.entrySet()) {
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

                sourceFullData.add(sourceData);
            }
        }

        return sourceFullData;
    }

    private DiffDetail diff(Long batchId, MappingRule mappingRule, boolean checkInAdvance, Object sourceValue, String sourceSql,
                            Object targetValue, String targetSql) {
        log.info(String.format("source sql: %s, source value: %s; target sql: %s, target value: %s",
            sourceSql, sourceValue, targetSql, targetValue));

        // 部分特殊的字段需要提前校验，和提前结束
        if (checkInAdvance) {
            boolean isFieldCheckPass = executeFieldCheck(sourceValue, targetValue, mappingRule);
            if (!isFieldCheckPass) {
                String diffType = DiffTypeConstant.VALUE_CHECK_FAIL;
                DiffDetail detail = buildDiffDetail(batchId, mappingRule, sourceValue, sourceSql, targetValue, targetSql, diffType, null);
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
            DiffDetail detail = buildDiffDetail(batchId, mappingRule, sourceValue, sourceSql, targetValue, targetSql, diffType, null);
            return detail;
        }

        // 都存在，检查一致性
        boolean isFieldCheckPass = executeFieldCheck(sourceValue, targetValue, mappingRule);
        if (!isFieldCheckPass) {
            diffType = DiffTypeConstant.VALUE_CHECK_FAIL;
            DiffDetail detail = buildDiffDetail(batchId, mappingRule, sourceValue, sourceSql, targetValue, targetSql, diffType, null);
            return detail;
        }

        return null;
    }

    private DiffDetail buildDiffDetail(Long batchId, MappingRule mappingRule, Object sourceValue, String sourceSql,
                                       Object targetValue, String targetSql, String diffType, String errorMessage) {
        DiffDetail diffDetail = DiffDetail.builder()
            .batchId(batchId)
            .diffType(diffType)
            .sourceQuerySql(sourceSql)
            .sourceTableName(mappingRule.getSourceMappingItem().getTableName())
            .sourceFieldName(mappingRule.getSourceMappingItem().getFieldName())
            .sourceValue(sourceValue == null ? null : String.valueOf(sourceValue))
            .targetQuerySql(targetSql)
            .targetTableName(mappingRule.getTargetMappingItem().getTableName())
            .targetFieldName(mappingRule.getTargetMappingItem().getFieldName())
            .targetValue(targetValue == null ? null : String.valueOf(targetValue))
            .errorMessage(errorMessage)
            .build();

        return diffDetail;
    }

    private String buildTargetSql(String targetTable, String targetField, MigrationData sourceData, String sourcePrimaryKey,
                                  String sourceTable, MigrationConfig migrationConfig) {
        String sourceId = String.valueOf(sourceData.getValue(sourcePrimaryKey));

        Map<String, SourceLocator> tableAndLocatorMap = migrationConfig.getTableAndLocatorMethodMap();
        SourceLocator locator = tableAndLocatorMap.get(targetTable);
        String locateField = locator.getLocateField();

        CustomizedMethod customizedMethod = locator.getLocateMethod();
        // 没有自定义，有单独字段承载
        if (customizedMethod == null || StringUtils.isEmpty(customizedMethod.getBeanName())) {
            return String.format("%s = \"%s\"", locateField, sourceId);
        }

        String locateBeanName = customizedMethod.getBeanName();
        SourceLocateExt locateBean = SpringUtils.getLocateBean(locateBeanName);
        if (locateBean == null) {
            return String.format("%s = \"%s\"", locateField, sourceId);
        }

        String whereClause = locateBean.locateSource(locateField, sourceTable, sourceId, customizedMethod.getArgs());
        String sql = String.format("select %s from %s where %s", targetField, targetTable, whereClause);
        return sql;
    }

    private MigrationData convertData(MigrationData sourceData, MigrationConfig migrationConfig) {
        String sourceTableName = sourceData.getTableName();
        String targetTableName = null;

        MigrationData targetData = new MigrationData();
        for (Map.Entry<String, Object> sourceEntry : sourceData.getFieldAndValueMap().entrySet()) {
            String sourceFieldName = sourceEntry.getKey();

            MappingRule mappingRule = migrationConfig.getMappingRule(sourceTableName, sourceFieldName);
            TargetMappingItem targetMappingItem = mappingRule.getTargetMappingItem();

            /**
             * 针对MigrationData的fieldAndValueMap，不同sourceTableName和sourceFieldName转换之后的target table需要保持不变
             */
            String t = targetMappingItem.getTableName();
            if (targetTableName == null) {
                targetTableName = t;
            }
            Assert.isTrue(Objects.equals(targetTableName, t), "target table name changed during MigrationData convert, please check");
            targetData.setTableName(targetTableName);

            Object targetValue = getMappingValue(sourceEntry.getValue(), mappingRule);
            targetData.addOneField(targetMappingItem.getFieldName(), targetValue);
        }

        return targetData;
    }

    private Object getMappingValue(Object sourceValue, MappingRule mappingRule) {
        // 没有自定义，平迁
        if (mappingRule.getFieldCheckMethod() == null || StringUtils.isEmpty(mappingRule.getFieldCheckMethod().getBeanName())) {
            return sourceValue;
        }

        String fcBeanName = mappingRule.getFieldCheckMethod().getBeanName();
        FieldCheckExt fcBean = SpringUtils.getFcBean(fcBeanName);
        if (fcBean == null) {
            return sourceValue;
        }

        FieldCheckResult fcResult = fcBean.check(sourceValue, null, mappingRule.getFieldCheckMethod().getArgs());
        return fcResult.getComputedValue();
    }

    private boolean executeFieldCheck(Object sourceValue, Object targetValue, MappingRule mappingRule) {
        // 没有自定义，平迁
        if (mappingRule.getFieldCheckMethod() == null || StringUtils.isEmpty(mappingRule.getFieldCheckMethod().getBeanName())) {
            return CommonUtils.generalEquals(sourceValue, targetValue);
        }

        String fcBeanName = mappingRule.getFieldCheckMethod().getBeanName();
        FieldCheckExt fcBean = SpringUtils.getFcBean(fcBeanName);
        if (fcBean == null) {
            return CommonUtils.generalEquals(sourceValue, targetValue);
        }

        // 有自定义处理逻辑
        FieldCheckResult fcResult = fcBean.check(sourceValue, targetValue, mappingRule.getFieldCheckMethod().getArgs());
        return fcResult.isPass();
    }

    private Map<String, String> getPrimaryKeyMap(MigrationConfig migrationConfig) {
        Map<String, String> sourceTableAndPrimaryKeyMap = new HashMap<>();

        for (MappingRule mappingRule : migrationConfig.getMappingRuleList()) {
            SourceMappingItem sourceMappingItem = mappingRule.getSourceMappingItem();

            if (mappingRule.getSourceMappingItem().isPrimaryKey()) {
                sourceTableAndPrimaryKeyMap.put(sourceMappingItem.getTableName(), sourceMappingItem.getFieldName());
            }
        }

        return sourceTableAndPrimaryKeyMap;
    }

}
