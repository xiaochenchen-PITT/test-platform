package com.cxc.test.platform.infra.mapper.master;

import com.cxc.test.platform.infra.domain.diff.DiffResultPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface DiffResultMapper {

    int insert(DiffResultPO diffResultPO);

    int update(DiffResultPO diffResultPO);

    List<DiffResultPO> getByConfigId(Long configId);

    DiffResultPO getByBatchId(Long batchId);

    int deleteByConfigId(Long configId);
}
