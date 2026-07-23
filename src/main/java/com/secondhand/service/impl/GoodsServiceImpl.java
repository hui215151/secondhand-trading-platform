package com.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.secondhand.entity.Goods;
import com.secondhand.mapper.GoodsMapper;
import com.secondhand.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String GOODS_CACHE_PREFIX = "goods:hot:";
    private static final String GOODS_LIST_CACHE = "goods:list:";

    @Override
    public IPage<Goods> listGoods(Page<Goods> page, String keyword, Integer categoryId,
                                   BigDecimal minPrice, BigDecimal maxPrice) {
        String cacheKey = GOODS_LIST_CACHE + keyword + ":" + categoryId + ":" + minPrice + ":" + maxPrice + ":" + page.getCurrent();

        IPage<Goods> cachedPage = (IPage<Goods>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedPage != null) {
            return cachedPage;
        }

        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("title", keyword);
        }
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        if (minPrice != null) {
            wrapper.ge("price", minPrice);
        }
        if (maxPrice != null) {
            wrapper.le("price", maxPrice);
        }

        wrapper.orderByDesc("create_time");

        IPage<Goods> result = goodsMapper.selectPage(page, wrapper);
        redisTemplate.opsForValue().set(cacheKey, result, 5, TimeUnit.MINUTES);

        return result;
    }

    @Override
    public Goods getGoodsDetail(Long goodsId) {
        String cacheKey = GOODS_CACHE_PREFIX + goodsId;
        Goods goods = (Goods) redisTemplate.opsForValue().get(cacheKey);
        if (goods != null) {
            goodsMapper.incrementViewCount(goodsId);
            return goods;
        }

        goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }

        redisTemplate.opsForValue().set(cacheKey, goods, 10, TimeUnit.MINUTES);
        goodsMapper.incrementViewCount(goodsId);

        return goods;
    }

    @Override
    public void publishGoods(Goods goods) {
        goods.setStatus(0);
        goods.setViewCount(0);
        goods.setFavoriteCount(0);
        goodsMapper.insert(goods);
    }

    @Override
    public void updateGoods(Goods goods) {
        redisTemplate.delete(GOODS_CACHE_PREFIX + goods.getId());
        goodsMapper.updateById(goods);
        redisTemplate.delete(GOODS_CACHE_PREFIX + goods.getId());
        redisTemplate.delete(GOODS_LIST_CACHE + "*");
    }

    @Override
    public void deleteGoods(Long goodsId, Long userId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!goods.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该商品");
        }

        goodsMapper.deleteById(goodsId);
        redisTemplate.delete(GOODS_CACHE_PREFIX + goodsId);
        redisTemplate.delete(GOODS_LIST_CACHE + "*");
    }
}