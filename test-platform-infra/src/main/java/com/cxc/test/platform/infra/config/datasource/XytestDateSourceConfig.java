package com.cxc.test.platform.infra.config.datasource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * 业务DB
 */
@Configuration
@MapperScan(basePackages = "com.cxc.test.platform.infra.mapper.xytest", sqlSessionFactoryRef = "xytestSqlSessionFactory")
public class XytestDateSourceConfig {

    @Primary // 表示这个数据源是默认数据源, 这个注解必须要加，因为不加的话spring将分不清楚那个为主数据源（默认数据源）
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.xytest") // 读取application.properties中的配置参数映射成为一个对象
    public DataSource xytestDataSource(){
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    public SqlSessionFactory xytestSqlSessionFactory(@Qualifier("xytestDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        // mapper的xml形式文件位置必须要配置，不然将报错：no statement （这种错误也可能是mapper的xml中，namespace与项目的路径不一致导致）
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/xytest/*.xml"));
        return bean.getObject();
    }

    @Primary
    @Bean
    public SqlSessionTemplate xytestSqlSessionTemplate(@Qualifier("xytestSqlSessionFactory") SqlSessionFactory sqlSessionFactory){
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
