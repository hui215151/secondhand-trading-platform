package com.secondhand.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.seckill.entity.SeckillGoods;
import com.secondhand.seckill.mapper.SeckillGoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询秒杀活动商品列表
 */
@Component
public class ListSeckillGoodsTool implements AgentTool {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Override
    public String name() {
        return "list_seckill_goods";
    }

    @Override
    public String description() {
        return "查询当前限时秒杀活动的商品列表，包含秒杀价、库存、活动起止时间";
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
        List<SeckillGoods> list = seckillGoodsMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SeckillGoods g : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", g.getId());
            item.put("goodsName", g.getGoodsName());
            item.put("category", g.getCategory());
            item.put("originalPrice", g.getOriginalPrice());
            item.put("seckillPrice", g.getSeckillPrice());
            item.put("stockCount", g.getStockCount());
            item.put("startTime", g.getStartTime() == null ? null : g.getStartTime().format(FMT));
            item.put("endTime", g.getEndTime() == null ? null : g.getEndTime().format(FMT));
            result.add(item);
        }
        return result;
    }
}
