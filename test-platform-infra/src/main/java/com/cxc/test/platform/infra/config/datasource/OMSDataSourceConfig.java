package com.cxc.test.platform.infra.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * 业务DB，不用了放这做个多数据源的例子
 */
@Configuration
@MapperScan(basePackages = "com.cxc.test.platform", sqlSessionFactoryRef = "omsSqlSessionFactory")
public class OMSDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.oms") //读取application.yml中的配置参数映射成为一个对象
    public DataSource omsDataSource(){
        HikariDataSource hikariDataSource = DataSourceBuilder.create().type(HikariDataSource.class).build();
        hikariDataSource.setConnectionTimeout(200000);
        hikariDataSource.setMaxLifetime(700000);
        hikariDataSource.setMaximumPoolSize(100);
        return hikariDataSource;
    }

    @Bean
    public SqlSessionFactory omsSqlSessionFactory(@Qualifier("omsDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        // mapper的xml形式文件位置必须要配置，不然将报错：no statement （这种错误也可能是mapper的xml中，namespace与项目的路径不一致导致）
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:/mapper/oms/*.xml"));
        return bean.getObject();
    }

    @Bean
    public SqlSessionTemplate omsSqlSessionTemplate(@Qualifier("omsSqlSessionFactory") SqlSessionFactory sqlSessionFactory){
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
