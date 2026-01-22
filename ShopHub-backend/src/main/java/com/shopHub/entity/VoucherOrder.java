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
@TableName("tb_voucher_order")
public class VoucherOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * PK
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * User id who placed the order
     */
    private Long userId;

    /**
     * Voucher id purchased
     */
    private Long voucherId;

    /**
     * Payment method: 1: balance payment; 2: Alipay; 3: WeChat
     */
    private Integer payType;

    /**
     * Order status: 1: unpaid; 2: paid; 3: redeemed; 4: cancelled; 5: refunding; 6: refunded
     */
    private Integer status;

    /**
     * Order time
     */
    private LocalDateTime createTime;

    /**
     * Payment time
     */
    private LocalDateTime payTime;

    /**
     * Redemption time
     */
    private LocalDateTime useTime;

    /**
     * Refund time
     */
    private LocalDateTime refundTime;

    /**
     * Update time
     */
    private LocalDateTime updateTime;


}
