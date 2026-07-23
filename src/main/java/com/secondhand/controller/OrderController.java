package com.secondhand.controller;

import com.secondhand.dto.Result;
import com.secondhand.entity.Order;
import com.secondhand.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<Order> create(@RequestParam Long goodsId,
                                 @RequestParam String address,
                                 @RequestParam(required = false) String remark,
                                 HttpServletRequest request) {
        Long buyerId = (Long) request.getAttribute("userId");
        return Result.success(orderService.createOrder(buyerId, goodsId, address, remark));
    }

    @PostMapping("/pay/{orderNo}")
    public Result<Void> pay(@PathVariable String orderNo) {
        orderService.payOrder(orderNo);
        return Result.success();
    }

    @PostMapping("/cancel/{orderNo}")
    public Result<Void> cancel(@PathVariable String orderNo, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.cancelOrder(orderNo, userId);
        return Result.success();
    }
}