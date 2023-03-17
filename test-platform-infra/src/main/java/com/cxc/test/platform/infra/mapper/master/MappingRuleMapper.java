package com.cxc.test.platform.infra.mapper.master;

import com.cxc.test.platform.infra.domain.migrationcheck.MappingRulePO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface MappingRuleMapper {

    int insertBatch(List<MappingRulePO> mappingRulePOList);

    List<MappingRulePO> getByConfigId(Long configId);

    MappingRulePO getById(Long id);

    int update(MappingRulePO mappingRulePO);

    int delete(Long id);

    int deleteByConfigId(Long configId);
}
