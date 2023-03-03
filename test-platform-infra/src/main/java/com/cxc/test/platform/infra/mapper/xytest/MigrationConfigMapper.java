package com.cxc.test.platform.infra.mapper.xytest;

import com.cxc.test.platform.infra.domain.migrationcheck.MigrationConfigPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface MigrationConfigMapper {

    // TODO: 2023/3/3 db链接的密码需要加密存储
    int insert(MigrationConfigPO migrationConfigPO);

    MigrationConfigPO getByConfigId(Long configId);
}
