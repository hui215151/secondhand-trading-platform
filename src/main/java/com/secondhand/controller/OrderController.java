package com.secondhand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    public Result<Void> pay(@PathVariable String orderNo, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.payOrder(orderNo, userId);
        return Result.success();
    }

    @PostMapping("/cancel/{orderNo}")
    public Result<Void> cancel(@PathVariable String orderNo, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.cancelOrder(orderNo, userId);
        return Result.success();
    }

    /**
     * 我的订单：role=buyer 我买到的 / seller 我卖出的 / all 全部
     */
    @GetMapping("/list")
    public Result<IPage<Order>> list(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size,
                                     @RequestParam(defaultValue = "all") String role,
                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.listOrders(new Page<>(page, size), userId, role));
    }
}
