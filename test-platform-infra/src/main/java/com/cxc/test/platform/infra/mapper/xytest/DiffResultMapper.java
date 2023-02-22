package com.cxc.test.platform.infra.mapper.xytest;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.cxc.test.platform.infra.domain.diff.DiffResultPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@DS("xytest")
@Mapper
@Component
public interface DiffResultMapper {

    int insert(DiffResultPO diffResultPO);
}
