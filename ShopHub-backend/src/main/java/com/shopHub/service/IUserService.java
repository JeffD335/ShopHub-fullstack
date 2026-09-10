package com.shopHub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shopHub.dto.LoginFormDTO;
import com.shopHub.dto.Result;
import com.shopHub.entity.User;

import javax.servlet.http.HttpSession;

public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result logout(String token);

    Result sign();

    Result signCount();
}
