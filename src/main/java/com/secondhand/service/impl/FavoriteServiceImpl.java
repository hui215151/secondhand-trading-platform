package com.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.secondhand.entity.Favorite;
import com.secondhand.entity.Goods;
import com.secondhand.mapper.FavoriteMapper;
import com.secondhand.mapper.GoodsMapper;
import com.secondhand.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void addFavorite(Long userId, Long goodsId) {
        Long count = favoriteMapper.selectCount(
                new QueryWrapper<Favorite>()
                        .eq("user_id", userId)
                        .eq("goods_id", goodsId)
        );
        if (count > 0) {
            throw new RuntimeException("已经收藏过了");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setGoodsId(goodsId);
        favoriteMapper.insert(favorite);

        goodsMapper.incrementFavoriteCount(goodsId);
        redisTemplate.delete("goods:hot:" + goodsId);
    }

    @Override
    public void removeFavorite(Long userId, Long goodsId) {
        favoriteMapper.delete(
                new QueryWrapper<Favorite>()
                        .eq("user_id", userId)
                        .eq("goods_id", goodsId)
        );

        goodsMapper.decrementFavoriteCount(goodsId);
        redisTemplate.delete("goods:hot:" + goodsId);
    }

    @Override
    public IPage<Goods> listFavorites(Page<Goods> page, Long userId) {
        List<Favorite> favorites = favoriteMapper.selectList(
                new QueryWrapper<Favorite>().eq("user_id", userId)
                        .orderByDesc("create_time")
        );

        if (favorites.isEmpty()) {
            return new Page<>();
        }

        List<Long> goodsIds = favorites.stream()
                .map(Favorite::getGoodsId)
                .collect(Collectors.toList());

        List<Goods> goodsList = goodsMapper.selectBatchIds(goodsIds);

        Page<Goods> resultPage = new Page<>();
        resultPage.setRecords(goodsList);
        resultPage.setTotal(goodsList.size());

        return resultPage;
    }
}