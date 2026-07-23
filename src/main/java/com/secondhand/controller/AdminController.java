package com.secondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.secondhand.dto.Result;
import com.secondhand.entity.Goods;
import com.secondhand.entity.User;
import com.secondhand.mapper.GoodsMapper;
import com.secondhand.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @GetMapping("/users")
    public Result<List<User>> listUsers() {
        return Result.success(userMapper.selectList(null));
    }

    @PostMapping("/goods/audit/{goodsId}")
    public Result<Void> auditGoods(@PathVariable Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }
        goods.setStatus(1);
        goodsMapper.updateById(goods);
        return Result.success();
    }
}