package com.cxc.test.platform.infra.mapper.master;

import com.cxc.test.platform.infra.domain.toolcenter.ToolPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface ToolMapper {

    List<ToolPO> selectByCondition(ToolPO toolPO);

    int insert(ToolPO toolPO);

    ToolPO update(ToolPO toolPO);
}
