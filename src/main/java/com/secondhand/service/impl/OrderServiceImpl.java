package com.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.secondhand.entity.Goods;
import com.secondhand.entity.Order;
import com.secondhand.mapper.GoodsMapper;
import com.secondhand.mapper.OrderMapper;
import com.secondhand.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long buyerId, Long goodsId, String address, String remark) {
        String repeatKey = "order:repeat:" + buyerId + ":" + goodsId;
        Boolean isFirst = redisTemplate.opsForValue()
                .setIfAbsent(repeatKey, "1", 5, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isFirst)) {
            throw new RuntimeException("请勿重复提交订单");
        }

        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null || goods.getStatus() != 1) {
            throw new RuntimeException("商品不存在或已下架");
        }

        if (goods.getUserId().equals(buyerId)) {
            throw new RuntimeException("不能购买自己的商品");
        }

        String orderNo = "SH" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setBuyerId(buyerId);
        order.setSellerId(goods.getUserId());
        order.setGoodsId(goodsId);
        order.setGoodsTitle(goods.getTitle());
        order.setPrice(goods.getPrice());
        order.setStatus(0);
        order.setAddress(address);
        order.setRemark(remark);

        orderMapper.insert(order);

        goods.setStatus(3);
        goodsMapper.updateById(goods);

        redisTemplate.delete("goods:hot:" + goodsId);
        redisTemplate.delete("goods:list:*");

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(String orderNo) {
        Order order = orderMapper.selectOne(
                new QueryWrapper<Order>().eq("order_no", orderNo)
        );
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态异常");
        }

        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo, Long userId) {
        Order order = orderMapper.selectOne(
                new QueryWrapper<Order>().eq("order_no", orderNo)
        );
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权取消该订单");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态不允许取消");
        }

        Goods goods = goodsMapper.selectById(order.getGoodsId());
        goods.setStatus(1);
        goodsMapper.updateById(goods);

        order.setStatus(4);
        orderMapper.updateById(order);

        redisTemplate.delete("goods:hot:" + order.getGoodsId());
    }
}