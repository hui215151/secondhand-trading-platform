package com.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secondhand.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
    @Update("UPDATE goods SET view_count = view_count + 1 WHERE id = #{goodsId}")
    void incrementViewCount(@Param("goodsId") Long goodsId);

    @Update("UPDATE goods SET favorite_count = favorite_count + 1 WHERE id = #{goodsId}")
    void incrementFavoriteCount(@Param("goodsId") Long goodsId);

    @Update("UPDATE goods SET favorite_count = favorite_count - 1 WHERE id = #{goodsId}")
    void decrementFavoriteCount(@Param("goodsId") Long goodsId);
}