package com.cxc.test.platform.infra.mapper.master;

import com.cxc.test.platform.infra.domain.migrationcheck.MigrationConfigPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface MigrationConfigMapper {

    // TODO: 2023/3/3 db链接的密码需要加密存储
    int insert(MigrationConfigPO migrationConfigPO);

    MigrationConfigPO getByConfigId(Long configId);

    List<MigrationConfigPO> getAll();

    int update(MigrationConfigPO migrationConfigPO);

    int deleteByConfigId(Long configId);
}
