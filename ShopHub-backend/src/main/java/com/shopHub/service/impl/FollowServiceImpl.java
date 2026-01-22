package com.shopHub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shopHub.dto.Result;
import com.shopHub.dto.UserDTO;
import com.shopHub.entity.Follow;
import com.shopHub.entity.User;
import com.shopHub.mapper.FollowMapper;
import com.shopHub.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopHub.service.IUserService;
import com.shopHub.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  Service Implementation Class
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IUserService userService;

    /**
     * follow or cancel follow user
     * @param followUserId userId that to followed or not
     * @param isFollow status followed or not
     * @return ok()
     */
    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        // 1.get  user from current thread
        Long userId = UserHolder.getUser().getId();
        String key = "follows:" + userId;
        // 1.check if is followed or not
        if (isFollow) {
            // 2.follow, create a new Follow entity and save to DB
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = save(follow);
            // 2.1 add to redis set  SADD userId followUserId

            if (isSuccess){
                stringRedisTemplate.opsForSet().add(key, String.valueOf(followUserId));
            }

        } else {
            // 3. cancel follow. delete from tb_follow where user_id = ? and follow_user_id = ?
            boolean isSuccess =  remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId).eq("follow_user_id", followUserId));

            //3.1 remove from redis set
            if(isSuccess){
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
                            }
        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        Long userId = UserHolder.getUser().getId();
        // check if is followed, select count(*) from tb_follow where user_id = ? and follow_user_id = ?
        Integer count = query().eq("user_id", userId).eq("follow_user_id", followUserId).count();
        return Result.ok(count > 0);
    }

    @Override
    public Result commonFollow(Long id) {
        //1. get userId from current Thread
        Long userId = UserHolder.getUser().getId();
        //2. append key
        String key1 = "follows:" + userId;
        String key2 = "follows:" + id;
        //3. get intersect, k1 userid, k2 id
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key1, key2);
        //4. check if is null
        if(intersect == null || intersect.isEmpty()){
            //return empty list prevent null value
            return  Result.ok(Collections.emptyList());
        }
        //5. parse userId list
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        //6. query user list by id list and encapsulate into UserDTO
        List<UserDTO> userDTOS= userService.listByIds(ids)
                .stream().map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(userDTOS);
    }
}
