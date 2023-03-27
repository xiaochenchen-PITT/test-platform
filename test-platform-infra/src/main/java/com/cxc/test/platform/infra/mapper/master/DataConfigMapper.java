package com.cxc.test.platform.infra.mapper.master;

import com.cxc.test.platform.infra.domain.datacheck.DataConfigPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface DataConfigMapper {

    // TODO: 2023/3/3 db链接的密码需要加密存储
    int insert(DataConfigPO dataConfigPO);

    DataConfigPO getByConfigId(Long configId);

    List<DataConfigPO> getAll();

    int update(DataConfigPO dataConfigPO);

    int deleteByConfigId(Long configId);
}
