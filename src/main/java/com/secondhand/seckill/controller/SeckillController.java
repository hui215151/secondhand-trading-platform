package com.secondhand.seckill.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.secondhand.dto.Result;
import com.secondhand.seckill.entity.SeckillGoods;
import com.secondhand.seckill.entity.SeckillOrder;
import com.secondhand.seckill.mapper.SeckillGoodsMapper;
import com.secondhand.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/seckill")
public class SeckillController {

    /**
     * 令牌桶限流，QPS 可通过 seckill.rate-limit.qps 配置（默认 1000）。
     * 压测时可用 -Dseckill.rate-limit.qps=100000 临时放开。
     */
    @Value("${seckill.rate-limit.qps:1000}")
    private double rateLimitQps;

    private RateLimiter rateLimiter;

    @PostConstruct
    public void initRateLimiter() {
        this.rateLimiter = RateLimiter.create(rateLimitQps);
        log.info("秒杀限流已启用：{} QPS", rateLimitQps);
    }

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 获取秒杀商品列表（公开接口）
     */
    @GetMapping("/goods")
    public Result<List<SeckillGoods>> listGoods() {
        return Result.success(seckillGoodsMapper.selectList(null));
    }

    /**
     * 执行秒杀
     */
    @PostMapping("/do")
    public Result<SeckillOrder> doSeckill(HttpServletRequest request,
                                          @RequestParam Long goodsId) {
        Long userId = (Long) request.getAttribute("userId");

        // 1. 限流
        if (!rateLimiter.tryAcquire(1, TimeUnit.SECONDS)) {
            throw new RuntimeException("活动太火爆，请稍后重试");
        }

        // 2. 执行秒杀
        return Result.success(seckillService.doSeckill(userId, goodsId));
    }

    /**
     * 查询秒杀结果
     */
    @GetMapping("/result")
    public Result<SeckillOrder> getResult(HttpServletRequest request,
                                          @RequestParam Long goodsId) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(seckillService.getSeckillResult(userId, goodsId));
    }
}
