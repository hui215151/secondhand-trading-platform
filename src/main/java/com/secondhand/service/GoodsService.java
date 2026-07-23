package com.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.secondhand.entity.Goods;
import java.math.BigDecimal;

public interface GoodsService {
    IPage<Goods> listGoods(Page<Goods> page, String keyword, Integer categoryId, 
                           BigDecimal minPrice, BigDecimal maxPrice);
    Goods getGoodsDetail(Long goodsId);
    void publishGoods(Goods goods);
    void updateGoods(Goods goods);
    void deleteGoods(Long goodsId, Long userId);
}