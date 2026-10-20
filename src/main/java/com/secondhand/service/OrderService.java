package com.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.secondhand.entity.Order;

public interface OrderService {
    Order createOrder(Long buyerId, Long goodsId, String address, String remark);
    void payOrder(String orderNo, Long userId);
    void cancelOrder(String orderNo, Long userId);
    IPage<Order> listOrders(Page<Order> page, Long userId, String role);
}
