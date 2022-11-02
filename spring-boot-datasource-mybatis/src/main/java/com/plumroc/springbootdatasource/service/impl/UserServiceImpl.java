package com.plumroc.springbootdatasource.service.impl;


import com.plumroc.springbootdatasource.annotation.DataSourceSwitcher;
import com.plumroc.springbootdatasource.bo.UserAddBO;
import com.plumroc.springbootdatasource.bo.UserUpdateBO;
import com.plumroc.springbootdatasource.entity.UserInfo;
import com.plumroc.springbootdatasource.enums.DataSourceTypeEnum;
import com.plumroc.springbootdatasource.mapper.UserInfoMapper;
import com.plumroc.springbootdatasource.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author PlumRoc
 * @date 2022-11-01
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserInfoMapper userInfoMapper;

    /**
     * 新增人员
     * 指定使用主库
     *
     * @param addBO
     */
    @Override
    @DataSourceSwitcher(DataSourceTypeEnum.MASTER)
    public void addUser(UserAddBO addBO) {
        log.info("[ 新增人员 ] start param:{}", addBO);
        String userId = UUID.randomUUID().toString().replaceAll("-", "");

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setUserName(addBO.getUserName());
        userInfo.setRealName(addBO.getRealName());
        userInfo.setMobile(addBO.getMobile());
        userInfo.setCreateTime(new Date());
        userInfo.setUpdateTime(new Date());
        userInfo.setRemark(addBO.getRemark());
        //demo项目部分参数值写死
        userInfo.setUserPassword("123456");
        userInfo.setDelFlag(0);
        userInfoMapper.insert(userInfo);
        log.info("[ 新增人员 ] end userId:{},userName:{}", userId, addBO.getUserName());
    }

    /**
     * 修改人员信息
     *
     * @param updateBO
     */
    @Override
    @DataSourceSwitcher(DataSourceTypeEnum.MASTER)
    public void updateUser(UserUpdateBO updateBO) {
        log.info("[ 修改人员信息 ] start param:{}", updateBO);

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(updateBO.getUserId());
        userInfo.setUserName(updateBO.getUserName());
        userInfo.setRealName(updateBO.getRealName());
        userInfo.setMobile(updateBO.getMobile());
        userInfo.setCreateTime(new Date());
        userInfo.setUpdateTime(new Date());
        userInfo.setRemark(updateBO.getRemark());
        userInfoMapper.updateByUserIdSelective(userInfo);
        log.info("[ 修改人员信息 ] end userId:{},userName:{}", updateBO.getUserId(), updateBO.getUserName());
    }


    /**
     * 查询所有 人员信息
     *
     * @return 人员信息 列表
     */
    @Override
    @DataSourceSwitcher(DataSourceTypeEnum.SLAVE1)
    public List<UserInfo> getAll() {
        log.info("[ 查询所有人员列表 ] 指定使用从库1 ");
        return userInfoMapper.getAll();
    }

    /**
     * 根据业务主键ID查询
     *
     * @param userId 业务主键
     */
    @Override
    @DataSourceSwitcher(DataSourceTypeEnum.SLAVE1)
    public UserInfo getByUserId(String userId) {
        log.info("[ 根据业务主键ID查询 ] 指定使用从库1 userId:{}", userId);
        return userInfoMapper.getByUserId(userId);
    }
}