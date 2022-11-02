package com.plumroc.springbootdatasource.bo;

import lombok.Data;

/**
 * 人员新增参数
 *
 * @author PlumRoc
 * @date 2022-11-01
 */
@Data
public class UserUpdateBO {

    /**
     * 人员业务ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String userName;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * remark
     */
    private String remark;
}