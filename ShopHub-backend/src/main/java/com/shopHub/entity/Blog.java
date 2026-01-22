package com.shopHub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("tb_blog")
public class Blog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * shopId
     */
    private Long shopId;
    /**
     * userId
     */
    private Long userId;
    /**
     * user Icon
     */
    @TableField(exist = false)
    private String icon;
    /**
     * userName
     */
    @TableField(exist = false)
    private String name;
    /**
     * Liked?
     */
    @TableField(exist = false)
    private Boolean isLike;

    /**
     * title
     */
    private String title;

    /**
     * pic of blog，at most 9，saperate by ","
     */
    private String images;

    /**
     * text description of blog
     */
    private String content;

    /**
     * num of like
     */
    private Integer liked;

    /**
     * num of comments
     */
    private Integer comments;

    /**
     * create time
     */
    private LocalDateTime createTime;

    /**
     * update time
     */
    private LocalDateTime updateTime;


}
