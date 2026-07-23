package com.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.secondhand.entity.Goods;

public interface FavoriteService {
    void addFavorite(Long userId, Long goodsId);
    void removeFavorite(Long userId, Long goodsId);
    IPage<Goods> listFavorites(Page<Goods> page, Long userId);
}