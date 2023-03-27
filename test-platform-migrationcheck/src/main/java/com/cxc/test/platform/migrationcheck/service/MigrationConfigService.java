package com.cxc.test.platform.migrationcheck.service;

import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.common.utils.ErrorMessageUtils;
import com.cxc.test.platform.infra.config.MachineUtils;
import com.cxc.test.platform.infra.domain.diff.DiffResultPO;
import com.cxc.test.platform.infra.domain.migrationcheck.MappingRulePO;
import com.cxc.test.platform.infra.domain.migrationcheck.MigrationConfigPO;
import com.cxc.test.platform.infra.domain.migrationcheck.SourceInitSqlPO;
import com.cxc.test.platform.infra.domain.migrationcheck.SourceLocatorPO;
import com.cxc.test.platform.infra.mapper.master.*;
import com.cxc.test.platform.migrationcheck.converter.MigrationConfigConverter;
import com.cxc.test.platform.migrationcheck.domain.config.MigrationConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MigrationConfigService {

    @Resource
    MigrationConfigConverter migrationConfigConverter;

    @Resource
    MigrationConfigMapper migrationConfigMapper;

    @Resource
    MappingRuleMapper mappingRuleMapper;

    @Resource
    SourceInitSqlMapper sourceInitSqlMapper;

    @Resource
    SourceLocatorMapper sourceLocatorMapper;

    @Resource
    DiffResultMapper diffResultMapper;

    @Resource
    DiffDetailMapper diffDetailMapper;

    @Resource
    MachineUtils machineUtils;

    @Transactional
    public ResultDO<Long> addConfig(MigrationConfig migrationConfig) {
        try {
            int ret1 = migrationConfigMapper.insert(migrationConfigConverter.convertMigrationConfigPO(migrationConfig));
            Assert.isTrue(ret1 == 1, "failed to add migrationConfig");

            int ret2 = mappingRuleMapper.insertBatch(migrationConfigConverter.convertMappingRulePOList(migrationConfig));
            Assert.isTrue(ret2 >= 1, "failed to add mappingRule");

            int ret3 = sourceInitSqlMapper.insertBatch(migrationConfigConverter.convertSourceInitSqlPOList(migrationConfig));
            Assert.isTrue(ret3 >= 1, "failed to add sourceInitSql");

            int ret4 = sourceLocatorMapper.insertBatch(migrationConfigConverter.convertLocatorPOList(migrationConfig));
            Assert.isTrue(ret4 >= 1, "failed to add sourceLocator");

            return ResultDO.success(migrationConfig.getConfigId());
        } catch (Exception e) {
            log.error("addConfig failed. ", e);
//            return ResultDO.fail(ErrorMessageUtils.getMessage(e)); // 回滚必须要抛异常，这里不return了
            throw new RuntimeException("addConfig failed, please check log"); // 回滚默认需要抛RuntimeException或者Error类
        }
    }

    public ResultDO<Boolean> deleteConfig(Long configId, boolean isDeleteDiffResult) {
        try {
            int ret1 = migrationConfigMapper.deleteByConfigId(configId);
            Assert.isTrue(ret1 == 1, "failed to delete migrationConfig");

            int ret2 = mappingRuleMapper.deleteByConfigId(configId);
            Assert.isTrue(ret2 >= 1, "failed to delete mappingRule");

            int ret3 = sourceInitSqlMapper.deleteByConfigId(configId);
            Assert.isTrue(ret3 >= 1, "failed to delete sourceInitSql");

            int ret4 = sourceLocatorMapper.deleteByConfigId(configId);
            Assert.isTrue(ret4 >= 1, "failed to delete sourceLocator");

            if (isDeleteDiffResult) {
                int ret5 = diffResultMapper.deleteByConfigId(configId);
                Assert.isTrue(ret5 >= 1, "failed to delete diffResult");

                int ret6 = diffDetailMapper.deleteByConfigId(configId);
                Assert.isTrue(ret6 >= 1, "failed to delete diffDetail");
            }

            return ResultDO.success(Boolean.TRUE);
        } catch (Exception e) {
            log.error("deleteConfig failed. ", e);
//            return ResultDO.fail(ErrorMessageUtils.getMessage(e)); // 回滚必须要抛异常，这里不return了
            throw new RuntimeException("deleteConfig failed, please check log"); // 回滚默认需要抛RuntimeException或者Error类
        }
    }

    public ResultDO<MigrationConfig> getConfig(Long configId) {
        try {
            MigrationConfigPO migrationConfigPO = migrationConfigMapper.getByConfigId(configId);
            if (migrationConfigPO == null) {
                return ResultDO.fail("Did not find config, config id: " + configId);
            }

            List<MappingRulePO> mappingRulePOList = mappingRuleMapper.getByConfigId(configId);
            List<SourceInitSqlPO> sourceInitSqlPOList = sourceInitSqlMapper.getByConfigId(configId);
            List<SourceLocatorPO> sourceLocatorPOList = sourceLocatorMapper.getByConfigId(configId);

            MigrationConfig migrationConfig = migrationConfigConverter.convertPO2DO(migrationConfigPO, mappingRulePOList,
                sourceLocatorPOList, sourceInitSqlPOList);

            return ResultDO.success(migrationConfig);
        } catch (Exception e) {
            log.error("getConfig failed. ", e);
            return ResultDO.fail(ErrorMessageUtils.getMessage(e));
        }
    }

    /**
     *
     * @param configIdSearch
     * @return key:configId, value: batch（结果）列表
     */
    public ResultDO<Map<Long, List<DiffResultPO>>> getConfigList(Long configIdSearch) {
        try {
            List<MigrationConfigPO> migrationConfigPOList = new ArrayList<>();
            if (configIdSearch != null && configIdSearch > 0) {
                MigrationConfigPO migrationConfigPO = migrationConfigMapper.getByConfigId(configIdSearch);
                if (migrationConfigPO == null) {
                    log.info("Did not find config for config id: " + configIdSearch);
                    return ResultDO.success(new HashMap<>());
                }

                migrationConfigPOList.add(migrationConfigPO);
            } else {
                migrationConfigPOList = migrationConfigMapper.getAll();
                if (CollectionUtils.isEmpty(migrationConfigPOList)) {
                    return ResultDO.success(new HashMap<>());
                }
            }

            Map<Long, List<DiffResultPO>> configIdAndDiffResultMap = new HashMap<>();
            for (MigrationConfigPO migrationConfigPO : migrationConfigPOList) {
                Long configId = migrationConfigPO.getConfigId();
                List<DiffResultPO> diffResultPOList = diffResultMapper.getByConfigId(configId);
                configIdAndDiffResultMap.put(configId, diffResultPOList);
            }

            return ResultDO.success(configIdAndDiffResultMap);
        } catch (Exception e) {
            log.error("getConfigList failed. ", e);
            return ResultDO.fail(ErrorMessageUtils.getMessage(e));
        }
    }

    public ResultDO<List<String>> getSelfIps() {
        return ResultDO.success(machineUtils.getIps());
    }

}
