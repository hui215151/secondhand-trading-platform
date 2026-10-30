package com.secondhand.agent.tool;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.entity.Order;
import com.secondhand.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询当前用户的闲置订单
 */
@Component
public class MyOrdersTool implements AgentTool {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public String name() {
        return "get_my_orders";
    }

    @Override
    public String description() {
        return "查询当前登录用户作为买家的闲置商品订单列表（订单号、商品、金额、状态）";
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", new HashMap<String, Object>());
        return schema;
    }

    @Override
    public Object execute(Long userId, JsonNode args) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("buyer_id", userId)
               .orderByDesc("create_time")
               .last("limit 10");
        List<Order> list = orderMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Order o : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("orderNo", o.getOrderNo());
            item.put("goodsTitle", o.getGoodsTitle());
            item.put("price", o.getPrice());
            item.put("status", statusText(o.getStatus()));
            item.put("createTime", o.getCreateTime() == null ? null : o.getCreateTime().format(FMT));
            result.add(item);
        }
        return result;
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "待付款";
            case 1: return "已付款";
            case 2: return "已发货";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知";
        }
    }
}
