package com.cxc.test.platform.infra.mapper.master;

import com.cxc.test.platform.infra.domain.migrationcheck.SourceLocatorPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface SourceLocatorMapper {

    int insertBatch(List<SourceLocatorPO> sourceLocatorPOList);

    List<SourceLocatorPO> getByConfigId(Long configId);

    int update(SourceLocatorPO sourceLocatorPO);
}
