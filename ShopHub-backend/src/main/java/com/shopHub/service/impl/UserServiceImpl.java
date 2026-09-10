package com.shopHub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopHub.dto.LoginFormDTO;
import com.shopHub.dto.Result;
import com.shopHub.dto.UserDTO;
import com.shopHub.entity.User;
import com.shopHub.mapper.UserMapper;
import com.shopHub.service.IUserService;
import com.shopHub.utils.RegexUtils;
import com.shopHub.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.shopHub.utils.RedisConstants.*;
import static com.shopHub.utils.SystemConstants.USER_NICK_NAME_PREFIX;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1. Validate phone number.
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("Invalid phone number format");
        }
        // 2. Generate and store a short-lived verification code.
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY+ phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        log.debug("Verification code generated successfully: {}", code);
        return  Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String code = loginForm.getCode();
        String phone = loginForm.getPhone();
        // 1. Validate phone number.
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("Invalid phone number format");
        }
        // 2. Validate the verification code stored in Redis.
       String cacheCode =  stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY+ phone);
        if(cacheCode == null || !cacheCode.equals(code)){
            return Result.fail("Incorrect verification code");
        }
        // 3. Create the user on first login.
        User user = query().eq("phone", phone).one();
        if(user == null){
          user = createUserWithPhone(phone);
        }
        // 4. Store the compact login session in Redis.
        String token = UUID.randomUUID(false).toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
               CopyOptions.create()
                       .ignoreNullValue()
                       .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        stringRedisTemplate.delete(LOGIN_CODE_KEY + phone);
        return Result.ok(token);
    }

    @Override
    public Result logout(String token) {
        if (StrUtil.isNotBlank(token)) {
            stringRedisTemplate.delete(LOGIN_USER_KEY + token);
        }
        UserHolder.removeUser();
        return Result.ok();
    }

    @Override
    public Result sign() {
        // 1. Get current user.
        Long userId = UserHolder.getUser().getId();
        // 2. Build the monthly sign-in bitmap key.
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String key = USER_SIGN_KEY + userId + formattedDate;
        // 3. Mark today as signed in.
        int dayOfMonth = now.getDayOfMonth();
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        return Result.ok();
    }

    @Override
    public Result signCount() {
        // 1. Get current user and monthly bitmap key.
        Long userId = UserHolder.getUser().getId();
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String key = USER_SIGN_KEY + userId + formattedDate;
        // 2. Read all sign-in bits from day 1 through today.
        int dayOfMonth = now.getDayOfMonth();
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands
                                .BitFieldType.unsigned(dayOfMonth))
                        .valueAt(0));
        if(result == null || result.isEmpty()) return Result.ok();
        Long num = result.get(0);
        if(num == null || num == 0) return Result.ok();
        // 3. Count consecutive signed-in days from today backwards.
        int count = 0;
           while(true){
               if ((num & 1) == 0) {
                   break;
               }else {
                   count++;
                   num >>>= 1;
               }
           }
        return Result.ok(count);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
