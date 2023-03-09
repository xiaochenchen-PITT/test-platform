package com.cxc.test.platform.infra.mapper.master;

import com.cxc.test.platform.infra.domain.diff.DiffDetailPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface DiffDetailMapper {

    int insert(DiffDetailPO diffDetailPO);

    List<DiffDetailPO> getByBatchId(Long batchId);
}
