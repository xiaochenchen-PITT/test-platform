package com.cxc.test.platform.infra.mapper.xytest;

import com.cxc.test.platform.infra.domain.migrationcheck.MigrationConfigPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface MigrationConfigMapper {

    int insert(MigrationConfigPO migrationConfigPO);

    MigrationConfigPO getByConfigId(Long configId);
}
