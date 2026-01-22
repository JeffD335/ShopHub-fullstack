package com.shopHub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_blog_comments")
public class BlogComments implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * User id
     */
    private Long userId;

    /**
     * Blog id
     */
    private Long blogId;

    /**
     * Associated level 1 comment id, if it's a level 1 comment, the value is 0
     */
    private Long parentId;

    /**
     * Replied comment id
     */
    private Long answerId;

    /**
     * Reply content
     */
    private String content;

    /**
     * Number of likes
     */
    private Integer liked;

    /**
     * Status: 0: normal; 1: reported; 2: forbidden to view
     */
    private Boolean status;

    /**
     * Create time
     */
    private LocalDateTime createTime;

    /**
     * Update time
     */
    private LocalDateTime updateTime;


}
