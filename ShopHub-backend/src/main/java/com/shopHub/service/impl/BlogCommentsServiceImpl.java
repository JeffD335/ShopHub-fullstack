package com.shopHub.service.impl;

import com.shopHub.dto.Result;
import com.shopHub.entity.BlogComments;
import com.shopHub.mapper.BlogCommentsMapper;
import com.shopHub.service.IBlogCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopHub.service.IBlogService;
import com.shopHub.utils.UserHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {
    @Resource
    private IBlogService blogService;

    @Override
    @Transactional
    public Result addComment(BlogComments comment) {
        Long userId = UserHolder.getUser().getId();
        comment.setUserId(userId);
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        if (comment.getAnswerId() == null) {
            comment.setAnswerId(0L);
        }
        if (comment.getLiked() == null) {
            comment.setLiked(0);
        }
        if (comment.getStatus() == null) {
            comment.setStatus(0);
        }

        boolean saved = save(comment);
        if (!saved) {
            return Result.fail("Failed to create comment");
        }
        blogService.update()
                .setSql("comments = IFNULL(comments, 0) + 1")
                .eq("id", comment.getBlogId())
                .update();
        return Result.ok(comment.getId());
    }

    @Override
    public Result queryCommentsByBlogId(Long blogId) {
        List<BlogComments> comments = query()
                .eq("blog_id", blogId)
                .eq("status", 0)
                .orderByDesc("create_time")
                .list();
        return Result.ok(comments);
    }
}
