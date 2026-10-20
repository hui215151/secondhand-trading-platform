package com.secondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.secondhand.dto.Result;
import com.secondhand.entity.Goods;
import com.secondhand.entity.User;
import com.secondhand.exception.ForbiddenException;
import com.secondhand.mapper.GoodsMapper;
import com.secondhand.mapper.UserMapper;
import com.secondhand.utils.CacheHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private CacheHelper cacheHelper;

    @GetMapping("/users")
    public Result<List<User>> listUsers(HttpServletRequest request) {
        requireAdmin(request);

        List<User> users = userMapper.selectList(null);
        // 密码字段不返回给前端
        users.forEach(user -> user.setPassword(null));
        return Result.success(users);
    }

    @PostMapping("/goods/audit/{goodsId}")
    public Result<Void> auditGoods(@PathVariable Long goodsId, HttpServletRequest request) {
        requireAdmin(request);

        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }
        goods.setStatus(1);
        goodsMapper.updateById(goods);

        // 审核通过后商品会出现在列表里，清掉旧缓存
        cacheHelper.evictByPrefix("goods:list:");
        cacheHelper.evictByPrefix("goods:hot:");
        return Result.success();
    }

    @GetMapping("/goods/pending")
    public Result<List<Goods>> pendingGoods(HttpServletRequest request) {
        requireAdmin(request);
        return Result.success(goodsMapper.selectList(
                new QueryWrapper<Goods>().eq("status", 0).orderByDesc("create_time")
        ));
    }

    private void requireAdmin(HttpServletRequest request) {
        Object role = request.getAttribute("role");
        if (!(role instanceof Integer) || ((Integer) role) != 1) {
            throw new ForbiddenException("需要管理员权限");
        }
    }
}
