package com.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long goodsId;
    private String goodsTitle;
    private BigDecimal price;
    private Integer status;
    private String address;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime completeTime;
}