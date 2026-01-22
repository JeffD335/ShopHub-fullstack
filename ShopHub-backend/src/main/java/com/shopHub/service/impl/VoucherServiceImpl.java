package com.shopHub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopHub.dto.Result;
import com.shopHub.entity.Voucher;
import com.shopHub.mapper.VoucherMapper;
import com.shopHub.entity.SeckillVoucher;
import com.shopHub.service.ISeckillVoucherService;
import com.shopHub.service.IVoucherService;
import com.shopHub.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static com.shopHub.utils.RedisConstants.SECKILL_STOCK_KEY;

/**
 * <p>
 *  Service Implementation Class
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // Query voucher information
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // Return result
        return Result.ok(vouchers);
    }

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // save voucher
        save(voucher);
        // save detail info
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        // save stock of SeckillVoucher  into redis
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock().toString());
    }
}
