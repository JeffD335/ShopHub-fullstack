package com.shopHub.controller;


import com.shopHub.dto.Result;
import com.shopHub.service.IFollowService;
import com.shopHub.service.impl.FollowServiceImpl;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 *
 * </p>
 *
 * @author Jianfei
 * @since 2026-01-21
 */
@RestController
@RequestMapping("/follow")
public class FollowController {
    @Resource
    private IFollowService followService;

    //follow / cancel follow
    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long followUserId, @PathVariable("isFollow") Boolean isFollow) {
        return followService.follow(followUserId, isFollow);
    }
    //query if is followed
    @GetMapping("/status/{id}")
    public Result isFollow(@PathVariable("id") Long followUserId) {
        return followService.isFollow(followUserId);
    }
    //query common follower
    @GetMapping("/common/{id}")
    public Result commonFollow(@PathVariable("id") Long id){
        return followService.commonFollow(id);
    }
}
