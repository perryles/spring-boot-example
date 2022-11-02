package com.plumroc.springbootdatasource.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.plumroc.springbootdatasource.dynamic.DynamicDataSource;
import com.plumroc.springbootdatasource.enums.DataSourceTypeEnum;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据源配置
 *
 * @author PlumRoc
 * @date 2022-11-01
 */
@Configuration
public class DataSourceConfig {

    /**
     * 主数据源 (可读可写的主数据源)
     */
    @Primary
    @Bean(name = "masterDataSource")
    @Qualifier("masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        //指定连接池类型-DruidDataSource
        return DataSourceBuilder.create().type(DruidDataSource.class).build();
    }

    /**
     * 从数据源1(只读从数据源1)
     */
    @Bean(name = "slave1DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.slave1")
    public DataSource salve1DataSource() {
        return DataSourceBuilder.create().type(DruidDataSource.class).build();
    }

    /**
     * 动态数据源
     *
     * @param masterDataSource 可读可写主数据源
     * @param slave1DataSource 只读子数据源1
     */
    @Bean(name = "dynamicDataSource")
    public DataSource createDynamicDataSource(
            @Qualifier(value = "masterDataSource") final DataSource masterDataSource,
            @Qualifier(value = "slave1DataSource") final DataSource slave1DataSource) {
        //将所有数据源放到Map中
        Map<Object, Object> targetDataSources = new HashMap<>(4);
        targetDataSources.put(DataSourceTypeEnum.MASTER, masterDataSource);
        targetDataSources.put(DataSourceTypeEnum.SLAVE1, slave1DataSource);

        //动态数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        //设置默认数据源
        dynamicDataSource.setDefaultTargetDataSource(masterDataSource);
        //设置可通过路由key,切换的数据源Map集
        dynamicDataSource.setTargetDataSources(targetDataSources);
        return dynamicDataSource;
    }
}
