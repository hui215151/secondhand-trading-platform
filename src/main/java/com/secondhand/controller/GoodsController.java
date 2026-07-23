package com.secondhand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.secondhand.dto.Result;
import com.secondhand.entity.Goods;
import com.secondhand.service.GoodsService;
import com.secondhand.utils.OssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OssUtil ossUtil;

    @GetMapping("/list")
    public Result<IPage<Goods>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        Page<Goods> pageParam = new Page<>(page, size);
        return Result.success(goodsService.listGoods(pageParam, keyword, categoryId, minPrice, maxPrice));
    }

    @GetMapping("/detail/{goodsId}")
    public Result<Goods> detail(@PathVariable Long goodsId) {
        return Result.success(goodsService.getGoodsDetail(goodsId));
    }

    @PostMapping("/publish")
    public Result<Void> publish(@RequestBody Goods goods, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        goods.setUserId(userId);
        goodsService.publishGoods(goods);
        return Result.success();
    }

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        String url = ossUtil.upload(file);
        return Result.success(url);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Goods goods, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        goods.setUserId(userId);
        goodsService.updateGoods(goods);
        return Result.success();
    }

    @DeleteMapping("/delete/{goodsId}")
    public Result<Void> delete(@PathVariable Long goodsId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        goodsService.deleteGoods(goodsId, userId);
        return Result.success();
    }
}