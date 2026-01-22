package com.shopHub.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.shopHub.dto.UserDTO;
import com.shopHub.entity.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * only intercept url that need to be login
 * intercept only when user is not exist
 */
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
     //1.check if its need to be intercepted(check user in ThreadLocal)
        if(UserHolder.getUser() == null){
            response.setStatus(401);
            return false;
        }
        return true;
    }

}