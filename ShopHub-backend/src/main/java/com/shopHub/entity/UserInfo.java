package com.shopHub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_user_info")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key, user id
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * City name
     */
    private String city;

    /**
     * Personal introduction, should not exceed 128 characters
     */
    private String introduce;

    /**
     * Number of fans
     */
    private Integer fans;

    /**
     * Number of people followed
     */
    private Integer followee;

    /**
     * Gender: 0: male; 1: female
     */
    private Boolean gender;

    /**
     * Birthday
     */
    private LocalDate birthday;

    /**
     * Credits
     */
    private Integer credits;

    /**
     * Membership level, 0~9 levels, 0 means no membership
     */
    private Boolean level;

    /**
     * Create time
     */
    private LocalDateTime createTime;

    /**
     * Update time
     */
    private LocalDateTime updateTime;


}
