package com.shopHub.service;

import com.shopHub.dto.Result;
import com.shopHub.entity.BlogComments;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IBlogCommentsService extends IService<BlogComments> {

    Result addComment(BlogComments comment);

    Result queryCommentsByBlogId(Long blogId);
}
