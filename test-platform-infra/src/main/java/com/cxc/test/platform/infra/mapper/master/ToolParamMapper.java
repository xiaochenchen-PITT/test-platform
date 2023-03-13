package com.cxc.test.platform.infra.mapper.master;

import com.cxc.test.platform.infra.domain.toolcenter.ToolParamPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface ToolParamMapper {

    List<ToolParamPO> selectByCondition(ToolParamPO toolParamPO);

    int insert(ToolParamPO toolParamPO);

    int update(ToolParamPO toolParamPO);

    int delete(Long id);
}
