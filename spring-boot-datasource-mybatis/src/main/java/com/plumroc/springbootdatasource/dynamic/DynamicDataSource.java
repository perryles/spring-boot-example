package com.plumroc.springbootdatasource.dynamic;

import com.plumroc.springbootdatasource.enums.DataSourceTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 扩展动态-数据源
 *
 * @author PlumRoc
 * @date 2022-11-01
 */
@Slf4j
public class DynamicDataSource extends AbstractRoutingDataSource {

    /**
     * 通过路由Key切换数据源
     * <p>
     * spring 在开始进行数据库操作时会通过这个方法来决定使用哪个数据库，
     * 因此我们在这里调用 DynamicDataSourceContextHolder.getDataSourceType()方法获取当前操作类别,
     * 同时可进行读库的负载均衡
     */
    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceTypeEnum typeEnum = DynamicDataSourceContextHolder.getDataSourceType();
        log.info("[ Change data source ] >> " + typeEnum.name());
        return typeEnum;
    }

}