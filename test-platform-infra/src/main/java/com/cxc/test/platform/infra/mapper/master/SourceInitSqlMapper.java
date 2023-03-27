package com.cxc.test.platform.infra.mapper.master;

import com.cxc.test.platform.infra.domain.datacheck.SourceInitSqlPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface SourceInitSqlMapper {

    int insertBatch(List<SourceInitSqlPO> sourceInitSqlPOList);

    List<SourceInitSqlPO> getByConfigId(Long configId);

    int update(SourceInitSqlPO sourceInitSqlPO);

    int deleteByConfigId(Long configId);
}
