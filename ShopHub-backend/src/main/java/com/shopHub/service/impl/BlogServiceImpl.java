package com.shopHub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.shopHub.dto.Result;
import com.shopHub.dto.ScrollResult;
import com.shopHub.dto.UserDTO;
import com.shopHub.entity.Blog;
import com.shopHub.entity.Follow;
import com.shopHub.entity.User;
import com.shopHub.mapper.BlogMapper;
import com.shopHub.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopHub.service.IFollowService;
import com.shopHub.service.IUserService;
import com.shopHub.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.shopHub.utils.RedisConstants.BLOG_LIKED_KEY;
import static com.shopHub.utils.RedisConstants.FEED_KEY;

/**
 * <p>
 *  Service Implementation Class
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {
    @Resource
    private IUserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IFollowService followService;
    @Override
    public Result queryBlogById(Long id) {
        // 1.query blog
        Blog blog = getById(id);
        if (blog == null) {
            return Result.fail("Blog does not exist！");
        }
        // 2.query the user of blog
        queryBlogUser(blog);

        return Result.ok(blog);
    }

    @Override
    public Result queryBlogLikes(Long id) {
        String key = BLOG_LIKED_KEY + id;
        // 1.query top5 user that liked, zrange key 0 4
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (top5 == null || top5.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        // 2.parse user id
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",", ids);
        // 3.query user by user id, WHERE id IN ( 5 , 1 ) ORDER BY FIELD(id, 5, 1) Users who liked first are shown first.
        List<UserDTO> userDTOS = userService.query()
                .in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        // 4.return
        return Result.ok(userDTOS);
    }

    /**
     *  same can only like a blog once, like will be cancel if like again
     * @param id blog id
     * @return always return ok
     */
    @Override
    public Result likeBlog(Long id) {
            // 1.get user from current thread
            Long userId = UserHolder.getUser().getId();
            // 2.check if the user already liked this blog(in the redis zset)
            String key = BLOG_LIKED_KEY + id;
            Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
            if(score == null){
                //3.not in the zset, allowed to like
                //3.1 update the like number +1 in DB
                boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
                //3.2 save user into redis zset
                if(isSuccess){
                    stringRedisTemplate.opsForZSet().add(key,userId.toString(), System.currentTimeMillis());
                }
            }else{
                //4.if in the zset, cancel the like
                //4.1 update DB
                boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
                //4.2 remove user from Zset
                if(isSuccess){
                    stringRedisTemplate.opsForZSet().remove(key,userId.toString());
                }
            }
            return Result.ok();
    }

    /**
     * save blog and get user's fans(follow user) and add blog id into redis Zset.
     * @param blog
     * @return
     */
    @Override
    public Result saveBlog(Blog blog) {
        //1. get user from current Thread
        Long userId = UserHolder.getUser().getId();
        blog.setUserId(userId);
        //2. save into DB
        boolean isSuccess = save(blog);
        if(!isSuccess){
            return Result.fail("Failed to create blog!");
        }
        //3. query user's all fans, select * from tb_follow where follow_user_id = ?
        List<Follow> followerList = followService.query().eq("follow_user_id", userId).list();
        //4. get all follower's id
        for(Follow followers : followerList) {
            Long followerId = followers.getUserId();
            //4. append key and save into redis zset
            String key = FEED_KEY + userId;
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
        }
        return Result.ok(blog.getId());
    }

    /**
     * Query the current user's feed (inbox) using Redis ZSet and return results in scroll pagination style.
     * @param max the maximum timestamp boundary (usually the last minTime from previous page)
     * @param offset the number of items to skip when multiple items share the same timestamp
     * @return
     */
    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
        // 1. get current user
        Long userId = UserHolder.getUser().getId();
        // 2. Query the inbox (feed) from Redis ZSet:
        //    ZREVRANGEBYSCORE key max min WITHSCORES LIMIT offset count
        String key = FEED_KEY + userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 2);
        // 3. Return empty if no data
        if (typedTuples == null || typedTuples.isEmpty()) {
            return Result.ok();
        }
        // 4. Parse data: blogIds, minTime (timestamp), and the next offset
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0; // 2
        int os = 1; // 2
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) { // 5 4 4 2 2
            // 4.1 Get blogId (member)
            ids.add(Long.valueOf(tuple.getValue()));
            // 4.2 Get score (timestamp)
            long time = tuple.getScore().longValue();
            if(time == minTime){
                os++;
            }else{
                minTime = time;
                os = 1;
            }
        }
        // Calculate next offset for scrolling (handle duplicate timestamps)
        os = minTime == max ? os : os + offset;
        // 5. Query blogs by ids and keep the same order as in Redis
        String idStr = StrUtil.join(",", ids);
        List<Blog> blogs = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();

        for (Blog blog : blogs) {
            // 5.1 Fill blog author/user info
            queryBlogUser(blog);
            // 5.2 Check whether current user liked the blog
            isBlogLiked(blog);
        }

        // 6. Wrap and return scroll result
        ScrollResult r = new ScrollResult();
        r.setList(blogs);
        r.setOffset(os);
        r.setMinTime(minTime);

        return Result.ok(r);
    }

    private void isBlogLiked(Blog blog) {
        // 1.get user from current thread
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            // if user are not login, do not check liked
            return;
        }
        Long userId = user.getId();
        // 2.check if current user liked the blog
        String key = "blog:liked:" + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score != null);
    }
    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }
}
