package com.shopHub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopHub.dto.LoginFormDTO;
import com.shopHub.dto.Result;
import com.shopHub.dto.UserDTO;
import com.shopHub.entity.User;
import com.shopHub.mapper.UserMapper;
import com.shopHub.service.IUserService;
import com.shopHub.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.shopHub.utils.RedisConstants.*;
import static com.shopHub.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * Service Implementation Class
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1.validate phone number
        if(!RegexUtils.isCodeInvalid(phone)){
            return Result.fail("wrong phone number format");
        }
        //2.generate verification code
        String code = RandomUtil.randomNumbers(6);
        //3.save code into redies // set key value ex 300
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY+ phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        //4.send code
        log.debug("send verification code success. verification code: ", code);
        //5.return result.ok
        return  Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String code = loginForm.getCode();
        String phone = loginForm.getPhone();
        //1.validate phone number
        if(!RegexUtils.isCodeInvalid(phone)){
            return Result.fail("wrong phone number format");
        }
        //2.get code from redis and validate code
       String cacheCode =  stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY+ phone);
        if(cacheCode == null || !cacheCode.equals(code)){
            return Result.fail("Incorrect verification code！");
        }
        //3.query user by phone number
        User user = query().eq("phone", phone).one();
        //4.check if user exist,if not create new user and save into db
        if(user == null){
          user = creatUserWithPhone(phone);
        }
        //5.save user into redis
        //5.1 generate random token
        String token = UUID.randomUUID(false).toString(true);
        //5.2 convert user object to hash
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
               CopyOptions.create()
                       .ignoreNullValue()
                       .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        //5.3save
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        //5.4 set token expire time
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        //6.return token
        return Result.ok(token);
    }

    private User creatUserWithPhone(String phone) {
        //1.create a new user
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        //2. save user into db
        save(user);
        return user;
    }
}
