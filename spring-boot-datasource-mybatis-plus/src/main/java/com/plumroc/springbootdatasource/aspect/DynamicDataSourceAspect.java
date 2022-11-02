package com.plumroc.springbootdatasource.aspect;


import com.plumroc.springbootdatasource.annotation.DataSourceSwitcher;
import com.plumroc.springbootdatasource.dynamic.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 目标数据源注解
 * 注：@Order(-1)是为了保证该AOP在@Transactional之前执行
 *
 * @author PlumRoc
 * @date 2022-11-01
 */
@Aspect
@Order(-1)
@Slf4j
@Component
public class DynamicDataSourceAspect {


    /**
     * 拦截使用 @DataSourceSwitcher注解的方法，前置设置数据源
     *
     * @param point
     * @param ds
     */
    @Before("@annotation(ds)")
    public void changeDataSource(JoinPoint point, DataSourceSwitcher ds) {
        log.info("[ set DataSource ] >> {} > {}", ds.value(), point.getSignature());
        DynamicDataSourceContextHolder.setDataSourceType(ds.value());
    }

    /**
     * 拦截使用 @DataSourceSwitcher注解的方法，后置-移除数据源
     *
     * @param point
     * @param ds
     */
    @After("@annotation(ds)")
    public void restoreDataSource(JoinPoint point, DataSourceSwitcher ds) {
        log.info("[ remove DataSource ] >> {} > {}", ds.value(), point.getSignature());
        DynamicDataSourceContextHolder.removeDataSourceType();
    }
}
