package com.secondhand.interceptor;

import com.secondhand.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            throw new RuntimeException("请先登录");
        }

        token = token.substring(7);

        if (!JwtUtil.validateToken(token)) {
            response.setStatus(401);
            throw new RuntimeException("登录已过期，请重新登录");
        }

        String redisKey = "token:" + token;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            response.setStatus(401);
            throw new RuntimeException("登录已失效，请重新登录");
        }

        redisTemplate.expire(redisKey, 30, TimeUnit.MINUTES);

        Long userId = JwtUtil.getUserId(token);
        request.setAttribute("userId", userId);

        return true;
    }
}