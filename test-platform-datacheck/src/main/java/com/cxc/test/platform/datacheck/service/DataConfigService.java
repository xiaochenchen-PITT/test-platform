package com.cxc.test.platform.datacheck.service;

import com.cxc.test.platform.common.domain.ResultDO;
import com.cxc.test.platform.common.utils.ErrorMessageUtils;
import com.cxc.test.platform.infra.config.MachineUtils;
import com.cxc.test.platform.infra.domain.diff.DiffResultPO;
import com.cxc.test.platform.infra.domain.datacheck.MappingRulePO;
import com.cxc.test.platform.infra.domain.datacheck.DataConfigPO;
import com.cxc.test.platform.infra.domain.datacheck.SourceInitSqlPO;
import com.cxc.test.platform.infra.domain.datacheck.SourceLocatorPO;
import com.cxc.test.platform.infra.mapper.master.*;
import com.cxc.test.platform.datacheck.converter.DataConfigConverter;
import com.cxc.test.platform.datacheck.domain.config.DataConfig;
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
public class DataConfigService {

    @Resource
    DataConfigConverter dataConfigConverter;

    @Resource
    DataConfigMapper dataConfigMapper;

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
    public ResultDO<Long> addConfig(DataConfig dataConfig) {
        try {
            int ret1 = dataConfigMapper.insert(dataConfigConverter.convertConfigPO(dataConfig));
            Assert.isTrue(ret1 == 1, "failed to add dataConfig");

            int ret2 = mappingRuleMapper.insertBatch(dataConfigConverter.convertMappingRulePOList(dataConfig));
            Assert.isTrue(ret2 >= 1, "failed to add mappingRule");

            int ret3 = sourceInitSqlMapper.insertBatch(dataConfigConverter.convertSourceInitSqlPOList(dataConfig));
            Assert.isTrue(ret3 >= 1, "failed to add sourceInitSql");

            int ret4 = sourceLocatorMapper.insertBatch(dataConfigConverter.convertLocatorPOList(dataConfig));
            Assert.isTrue(ret4 >= 1, "failed to add sourceLocator");

            return ResultDO.success(dataConfig.getConfigId());
        } catch (Exception e) {
            log.error("addConfig failed. ", e);
//            return ResultDO.fail(ErrorMessageUtils.getMessage(e)); // 回滚必须要抛异常，这里不return了
            throw new RuntimeException("addConfig failed, please check log"); // 回滚默认需要抛RuntimeException或者Error类
        }
    }

    public ResultDO<Boolean> deleteConfig(Long configId, boolean isDeleteDiffResult) {
        try {
            int ret1 = dataConfigMapper.deleteByConfigId(configId);
            Assert.isTrue(ret1 == 1, "failed to delete dataConfig");

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

    public ResultDO<DataConfig> getConfig(Long configId) {
        try {
            DataConfigPO dataConfigPO = dataConfigMapper.getByConfigId(configId);
            if (dataConfigPO == null) {
                return ResultDO.fail("Did not find config, config id: " + configId);
            }

            List<MappingRulePO> mappingRulePOList = mappingRuleMapper.getByConfigId(configId);
            List<SourceInitSqlPO> sourceInitSqlPOList = sourceInitSqlMapper.getByConfigId(configId);
            List<SourceLocatorPO> sourceLocatorPOList = sourceLocatorMapper.getByConfigId(configId);

            DataConfig dataConfig = dataConfigConverter.convertPO2DO(dataConfigPO, mappingRulePOList,
                sourceLocatorPOList, sourceInitSqlPOList);

            return ResultDO.success(dataConfig);
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
            List<DataConfigPO> dataConfigPOList = new ArrayList<>();
            if (configIdSearch != null && configIdSearch > 0) {
                DataConfigPO dataConfigPO = dataConfigMapper.getByConfigId(configIdSearch);
                if (dataConfigPO == null) {
                    log.info("Did not find config for config id: " + configIdSearch);
                    return ResultDO.success(new HashMap<>());
                }

                dataConfigPOList.add(dataConfigPO);
            } else {
                dataConfigPOList = dataConfigMapper.getAll();
                if (CollectionUtils.isEmpty(dataConfigPOList)) {
                    return ResultDO.success(new HashMap<>());
                }
            }

            Map<Long, List<DiffResultPO>> configIdAndDiffResultMap = new HashMap<>();
            for (DataConfigPO dataConfigPO : dataConfigPOList) {
                Long configId = dataConfigPO.getConfigId();
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
