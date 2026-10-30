package com.secondhand.seckill.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.secondhand.seckill.entity.TransactionLog;
import com.secondhand.seckill.mapper.TransactionLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class TransactionCheckJob {

    @Autowired
    private TransactionLogMapper transactionLogMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String STOCK_KEY_PREFIX = "seckill:stock:";

    /**
     * 每 5 分钟扫描一次失败的事务，回补 Redis 库存
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void checkFailedTransaction() {
        QueryWrapper<TransactionLog> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 2)  // 失败状态
               .lt("create_time", LocalDateTime.now().minusMinutes(1)); // 1分钟前的

        List<TransactionLog> failedList = transactionLogMapper.selectList(wrapper);

        for (TransactionLog txLog : failedList) {
            try {
                // 解析 businessId 获取 goodsId
                String[] parts = txLog.getBusinessId().split(":");
                Long goodsId = Long.valueOf(parts[1]);

                // 回补 Redis 库存
                String stockKey = STOCK_KEY_PREFIX + goodsId;
                redisTemplate.opsForValue().increment(stockKey);

                // 按 transaction_id 更新事务表为已回补（避免 updateById 因 id 为空更新 0 行）
                transactionLogMapper.update(null, new UpdateWrapper<TransactionLog>()
                        .eq("transaction_id", txLog.getTransactionId())
                        .set("status", 3));

                log.info("库存回补成功：transactionId={}, goodsId={}",
                        txLog.getTransactionId(), goodsId);

            } catch (Exception e) {
                log.error("库存回补失败：{}", e.getMessage());
            }
        }
    }
}
