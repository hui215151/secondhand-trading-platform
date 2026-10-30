package com.secondhand.agent.tool;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.entity.Goods;
import com.secondhand.mapper.GoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索在售闲置商品
 */
@Component
public class SearchGoodsTool implements AgentTool {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public String name() {
        return "search_goods";
    }

    @Override
    public String description() {
        return "按关键词搜索平台上在售的二手闲置商品，返回商品列表（标题、价格等）";
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> keyword = new HashMap<>();
        keyword.put("type", "string");
        keyword.put("description", "搜索关键词，如 iPhone、教材、自行车");

        Map<String, Object> properties = new HashMap<>();
        properties.put("keyword", keyword);

        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        List<String> required = new ArrayList<>();
        required.add("keyword");
        schema.put("required", required);
        return schema;
    }

    @Override
    public Object execute(Long userId, JsonNode args) {
        String keyword = args.path("keyword").asText("");
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)
               .like("title", keyword)
               .orderByDesc("create_time")
               .last("limit 10");
        List<Goods> list = goodsMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Goods g : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", g.getId());
            item.put("title", g.getTitle());
            item.put("price", g.getPrice());
            item.put("originalPrice", g.getOriginalPrice());
            item.put("favoriteCount", g.getFavoriteCount());
            result.add(item);
        }
        return result;
    }
}
