package com.secondhand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.secondhand.dto.Result;
import com.secondhand.entity.Goods;
import com.secondhand.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/add/{goodsId}")
    public Result<Void> add(@PathVariable Long goodsId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        favoriteService.addFavorite(userId, goodsId);
        return Result.success();
    }

    @DeleteMapping("/remove/{goodsId}")
    public Result<Void> remove(@PathVariable Long goodsId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        favoriteService.removeFavorite(userId, goodsId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<IPage<Goods>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        Page<Goods> pageParam = new Page<>(page, size);
        return Result.success(favoriteService.listFavorites(pageParam, userId));
    }
}