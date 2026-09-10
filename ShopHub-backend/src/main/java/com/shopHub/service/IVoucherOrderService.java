package com.shopHub.service;

import com.shopHub.dto.Result;
import com.shopHub.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IVoucherOrderService extends IService<VoucherOrder> {
    Result seckillVoucher(Long voucherId);
    void createVoucherOrder(VoucherOrder voucherOrder);
}
