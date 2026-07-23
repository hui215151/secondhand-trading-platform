package com.secondhand.service;

import com.secondhand.entity.Order;

public interface OrderService {
    Order createOrder(Long buyerId, Long goodsId, String address, String remark);
    void payOrder(String orderNo);
    void cancelOrder(String orderNo, Long userId);
}