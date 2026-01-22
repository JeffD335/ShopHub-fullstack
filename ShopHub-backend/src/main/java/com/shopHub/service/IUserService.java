package com.shopHub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shopHub.dto.LoginFormDTO;
import com.shopHub.dto.Result;
import com.shopHub.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  Service Interface
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);
}
