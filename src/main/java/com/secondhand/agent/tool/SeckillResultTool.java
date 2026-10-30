package com.secondhand.agent.tool;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.seckill.entity.SeckillOrder;
import com.secondhand.seckill.mapper.SeckillOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询当前用户的秒杀订单结果
 */
@Component
public class SeckillResultTool implements AgentTool {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Override
    public String name() {
        return "get_my_seckill_result";
    }

    @Override
    public String description() {
        return "查询当前登录用户在某件秒杀商品上的抢购结果/订单状态";
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> goodsId = new HashMap<>();
        goodsId.put("type", "integer");
        goodsId.put("description", "秒杀商品ID");

        Map<String, Object> properties = new HashMap<>();
        properties.put("goodsId", goodsId);

        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        List<String> required = new ArrayList<>();
        required.add("goodsId");
        schema.put("required", required);
        return schema;
    }

    @Override
    public Object execute(Long userId, JsonNode args) {
        Long goodsId = args.path("goodsId").asLong();
        QueryWrapper<SeckillOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("goods_id", goodsId)
               .orderByDesc("create_time")
               .last("limit 1");
        SeckillOrder order = seckillOrderMapper.selectOne(wrapper);

        Map<String, Object> item = new HashMap<>();
        if (order == null) {
            item.put("result", "暂无该商品的秒杀订单");
            return item;
        }
        item.put("goodsName", order.getGoodsName());
        item.put("seckillPrice", order.getSeckillPrice());
        item.put("status", statusText(order.getStatus()));
        item.put("createTime", order.getCreateTime() == null ? null : order.getCreateTime().format(FMT));
        return item;
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "创建中（抢购处理中）";
            case 1: return "已支付";
            case 2: return "已取消";
            default: return "未知";
        }
    }
}
