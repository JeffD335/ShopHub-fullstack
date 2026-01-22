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
@TableName("tb_shop")
public class Shop implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Shop name
     */
    private String name;

    /**
     * Shop type id
     */
    private Long typeId;

    /**
     * Shop images, multiple images separated by ','
     */
    private String images;

    /**
     * Business district, e.g., Lujiazui
     */
    private String area;

    /**
     * Address
     */
    private String address;

    /**
     * Longitude
     */
    private Double x;

    /**
     * Latitude
     */
    private Double y;

    /**
     * Average price, integer value
     */
    private Long avgPrice;

    /**
     * Sales volume
     */
    private Integer sold;

    /**
     * Number of comments
     */
    private Integer comments;

    /**
     * Rating, 1~5 points, multiplied by 10 for storage to avoid decimals
     */
    private Integer score;

    /**
     * Business hours, e.g., 10:00-22:00
     */
    private String openHours;

    /**
     * Create time
     */
    private LocalDateTime createTime;

    /**
     * Update time
     */
    private LocalDateTime updateTime;


    @TableField(exist = false)
    private Double distance;
}
