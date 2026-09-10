package com.shopHub.service.impl;

import com.shopHub.entity.UserInfo;
import com.shopHub.mapper.UserInfoMapper;
import com.shopHub.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
