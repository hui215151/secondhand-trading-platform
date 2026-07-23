package com.secondhand.controller;

import com.secondhand.dto.LoginDTO;
import com.secondhand.dto.Result;
import com.secondhand.entity.User;
import com.secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<String> login(@Validated @RequestBody LoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        return Result.success(token);
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        userService.register(user);
        return Result.success();
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getUserInfo(userId));
    }
}