package com.cxc.test.platform.infra.mapper.xytest;

import com.cxc.test.platform.infra.domain.migrationcheck.TargetLocatorPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface TargetLocatorMapper {

    int insertBatch(List<TargetLocatorPO> targetLocatorPOList);

    List<TargetLocatorPO> getByConfigId(Long configId);
}
