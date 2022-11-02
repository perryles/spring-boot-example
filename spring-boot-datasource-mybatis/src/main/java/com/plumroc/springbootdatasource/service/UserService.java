package com.plumroc.springbootdatasource.service;

import com.plumroc.springbootdatasource.bo.UserAddBO;
import com.plumroc.springbootdatasource.bo.UserUpdateBO;
import com.plumroc.springbootdatasource.entity.UserInfo;

import java.util.List;

/**
 * 人员操作接口
 *
 * @author PlumRoc
 * @date 2022-11-01
 */
public interface UserService {

    /**
     * 新增人员
     *
     * @param addBO
     */
    void addUser(UserAddBO addBO);

    /**
     * 修改人员信息
     *
     * @param updateBO
     */
    void updateUser(UserUpdateBO updateBO);


    /**
     * 查询所有 人员信息
     *
     * @return 人员信息 列表
     */
    List<UserInfo> getAll();

    /**
     * 根据业务主键ID查询
     *
     * @param userId 业务主键
     */
    UserInfo getByUserId(String userId);


}
