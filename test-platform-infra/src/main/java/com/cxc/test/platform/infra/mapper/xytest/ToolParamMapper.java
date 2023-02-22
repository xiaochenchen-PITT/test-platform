package com.cxc.test.platform.infra.mapper.xytest;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.cxc.test.platform.infra.domain.toolcenter.ToolParamPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@DS("xytest")
@Mapper
@Component
public interface ToolParamMapper {

    List<ToolParamPO> selectByCondition(ToolParamPO toolParamPO);

    int insert(ToolParamPO toolParamPO);

    ToolParamPO update(ToolParamPO toolParamPO);
}
