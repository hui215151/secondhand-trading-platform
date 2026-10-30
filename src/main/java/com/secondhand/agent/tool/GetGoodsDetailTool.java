package com.secondhand.agent.tool;

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
 * 查询单个闲置商品详情
 */
@Component
public class GetGoodsDetailTool implements AgentTool {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public String name() {
        return "get_goods_detail";
    }

    @Override
    public String description() {
        return "按商品ID查询闲置商品的详细信息（描述、图片、浏览量、状态等）";
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> goodsId = new HashMap<>();
        goodsId.put("type", "integer");
        goodsId.put("description", "商品ID");

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
        Goods g = goodsMapper.selectById(goodsId);
        if (g == null) {
            Map<String, String> empty = new HashMap<>();
            empty.put("error", "商品不存在");
            return empty;
        }
        Map<String, Object> item = new HashMap<>();
        item.put("id", g.getId());
        item.put("title", g.getTitle());
        item.put("description", g.getDescription());
        item.put("price", g.getPrice());
        item.put("originalPrice", g.getOriginalPrice());
        item.put("images", g.getImages());
        item.put("viewCount", g.getViewCount());
        item.put("favoriteCount", g.getFavoriteCount());
        item.put("status", statusText(g.getStatus()));
        return item;
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "待审核";
            case 1: return "在售";
            case 2: return "已下架";
            case 3: return "已售出";
            default: return "未知";
        }
    }
}
