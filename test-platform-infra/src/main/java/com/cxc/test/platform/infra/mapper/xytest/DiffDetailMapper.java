package com.cxc.test.platform.infra.mapper.xytest;

import com.cxc.test.platform.infra.domain.diff.DiffDetailPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface DiffDetailMapper {

    int insert(DiffDetailPO diffDetailPO);
}
