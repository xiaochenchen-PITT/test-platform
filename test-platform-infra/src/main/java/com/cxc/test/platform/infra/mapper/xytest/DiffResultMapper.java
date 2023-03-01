package com.cxc.test.platform.infra.mapper.xytest;

import com.cxc.test.platform.infra.domain.diff.DiffResultPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface DiffResultMapper {

    int insert(DiffResultPO diffResultPO);
}
