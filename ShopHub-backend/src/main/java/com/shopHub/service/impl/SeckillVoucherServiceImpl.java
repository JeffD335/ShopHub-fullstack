package com.shopHub.service.impl;

import com.shopHub.entity.SeckillVoucher;
import com.shopHub.mapper.SeckillVoucherMapper;
import com.shopHub.service.ISeckillVoucherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Flash sale voucher table, one-to-one relationship with voucher Service Implementation Class
 * </p>
 *
 * @author 虎哥
 * @since 2022-01-04
 */
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements ISeckillVoucherService {

}
