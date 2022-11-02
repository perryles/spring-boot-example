package com.plumroc.springbootdatasource.annotation;

import com.plumroc.springbootdatasource.enums.DataSourceTypeEnum;

import java.lang.annotation.*;

/**
 * 目标数据源注解-作用于方法上
 *
 * @author PlumRoc
 * @date 2022-11-01
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSourceSwitcher {

    /**
     * 目标数据源枚举名称
     */
    DataSourceTypeEnum value();
}
