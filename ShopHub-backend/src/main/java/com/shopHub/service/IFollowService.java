package com.shopHub.service;

import com.shopHub.dto.Result;
import com.shopHub.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IFollowService extends IService<Follow> {

    Result follow(Long followUserId, Boolean isFollow);

    Result isFollow(Long followUserId);

    Result commonFollow(Long id);
}
