package com.shopHub.controller;


import com.shopHub.dto.Result;
import com.shopHub.entity.BlogComments;
import com.shopHub.service.IBlogCommentsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;

@RestController
@RequestMapping("/blog-comments")
public class BlogCommentsController {
    @Resource
    private IBlogCommentsService blogCommentsService;

    @PostMapping
    public Result addComment(@RequestBody BlogComments comment) {
        return blogCommentsService.addComment(comment);
    }

    @GetMapping("/of/blog/{blogId}")
    public Result queryCommentsByBlogId(@PathVariable("blogId") Long blogId) {
        return blogCommentsService.queryCommentsByBlogId(blogId);
    }
}
