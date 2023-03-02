package com.cxc.test.platform.infra.mapper.xytest;

import com.cxc.test.platform.infra.domain.migrationcheck.SourceInitSqlPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface SourceInitSqlMapper {

    int insertBatch(List<SourceInitSqlPO> sourceInitSqlPOList);

    List<SourceInitSqlPO> getByConfigId(Long configId);
}
