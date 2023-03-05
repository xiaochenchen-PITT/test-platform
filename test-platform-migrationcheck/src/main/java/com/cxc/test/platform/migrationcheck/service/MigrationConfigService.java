package com.cxc.test.platform.migrationcheck.service;

import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.common.utils.ErrorMessageUtils;
import com.cxc.test.platform.infra.domain.migrationcheck.MappingRulePO;
import com.cxc.test.platform.infra.domain.migrationcheck.MigrationConfigPO;
import com.cxc.test.platform.infra.domain.migrationcheck.SourceInitSqlPO;
import com.cxc.test.platform.infra.domain.migrationcheck.SourceLocatorPO;
import com.cxc.test.platform.infra.mapper.xytest.MappingRuleMapper;
import com.cxc.test.platform.infra.mapper.xytest.MigrationConfigMapper;
import com.cxc.test.platform.infra.mapper.xytest.SourceInitSqlMapper;
import com.cxc.test.platform.infra.mapper.xytest.SourceLocatorMapper;
import com.cxc.test.platform.migrationcheck.converter.MigrationConfigConverter;
import com.cxc.test.platform.migrationcheck.domain.config.MigrationConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;

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
            List<MappingRulePO> mappingRulePOList = mappingRuleMapper.getByConfigId(configId);
            List<SourceInitSqlPO> sourceInitSqlPOList = sourceInitSqlMapper.getByConfigId(configId);
            List<SourceLocatorPO> sourceLocatorPOList = sourceLocatorMapper.getByConfigId(configId);

            if (migrationConfigPO == null) {
                return ResultDO.fail("Did not find config, config id: " + configId);
            }

            MigrationConfig migrationConfig = migrationConfigConverter.convertPO2DO(migrationConfigPO, mappingRulePOList,
                    sourceLocatorPOList, sourceInitSqlPOList);

            return ResultDO.success(migrationConfig);
        } catch (Exception e) {
            log.error("getConfig failed. ", e);
            return ResultDO.fail(ErrorMessageUtils.getMessage(e));
        }
    }

}
