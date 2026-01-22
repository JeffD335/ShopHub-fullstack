package com.shopHub.controller;


import cn.hutool.core.bean.BeanUtil;
import com.shopHub.dto.LoginFormDTO;
import com.shopHub.dto.Result;
import com.shopHub.dto.UserDTO;
import com.shopHub.entity.User;
import com.shopHub.entity.UserInfo;
import com.shopHub.service.IUserInfoService;
import com.shopHub.service.IUserService;
import com.shopHub.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * <p>
 * Frontend Controller
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * Send phone verification code
     */
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        // Send SMS verification code and save it
      return userService.sendCode(phone, session);
    }

    /**
     * Login functionality
     * @param loginForm Login parameters, including phone number and verification code; or phone number and password
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session){
        // Implement login functionality
        return userService.login(loginForm, session);
    }

    /**
     * Logout functionality
     * @return void
     */
    @PostMapping("/logout")
    public Result logout(){
        // TODO Implement logout functionality
        return Result.fail("Function not implemented");
    }

    @GetMapping("/me")
    public Result me(){
        // Get current logged-in user and return
       UserDTO user = UserHolder.getUser();
       return Result.ok(user);
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // Query user details
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // No details found, should be the first time viewing details
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // Return
        return Result.ok(info);
    }

    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") Long userId){
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        return Result.ok(userDTO);
    }

    @PutMapping("/info")
    public Result updateInfo(@RequestBody UserInfo userInfo){
        // Get current user ID
        UserDTO user = UserHolder.getUser();
        userInfo.setUserId(user.getId());
        // Update user info
        userInfoService.updateById(userInfo);
        return Result.ok();
    }
}
