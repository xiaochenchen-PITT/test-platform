package com.cxc.test.platform.migrationcheck.service;

import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.common.utils.ErrorMessageUtils;
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

}
