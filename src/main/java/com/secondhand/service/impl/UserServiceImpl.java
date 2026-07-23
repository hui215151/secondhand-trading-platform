package com.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.secondhand.dto.LoginDTO;
import com.secondhand.entity.User;
import com.secondhand.mapper.UserMapper;
import com.secondhand.service.UserService;
import com.secondhand.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public String login(LoginDTO loginDTO) {
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", loginDTO.getUsername())
        );

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!user.getPassword().equals(loginDTO.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        redisTemplate.opsForValue().set("token:" + token, user.getId(), 30, TimeUnit.MINUTES);

        return token;
    }

    @Override
    public void register(User user) {
        Long count = userMapper.selectCount(
                new QueryWrapper<User>().eq("username", user.getUsername())
        );
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }
        userMapper.insert(user);
    }

    @Override
    public User getUserInfo(Long userId) {
        return userMapper.selectById(userId);
    }
}