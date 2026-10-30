package com.secondhand.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 本地事务表：秒杀下单的最终一致性保证
 */
@Data
public class TransactionLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String transactionId;
    private String businessType;
    private String businessId;
    private Integer status; // 0-处理中 1-成功 2-失败 3-已回补
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
