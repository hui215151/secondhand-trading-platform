package com.secondhand.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondhand.dto.Result;
import com.secondhand.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * JWT 登录拦截器。
 * 未登录/过期时直接返回 HTTP 401 + JSON，不再抛异常（旧实现抛异常会被全局异常处理吞成 HTTP 200）。
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {

        // 放行 CORS 预检请求
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return reject(response, "请先登录");
        }

        token = token.substring(7);

        if (!JwtUtil.validateToken(token)) {
            return reject(response, "登录已过期，请重新登录");
        }

        String redisKey = "token:" + token;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            return reject(response, "登录已失效，请重新登录");
        }

        // 滑动续期
        redisTemplate.expire(redisKey, 30, TimeUnit.MINUTES);

        Claims claims = JwtUtil.parseToken(token);
        request.setAttribute("userId", Long.valueOf(claims.getSubject()));
        Object role = claims.get("role");
        request.setAttribute("role", role == null ? 0 : Integer.valueOf(role.toString()));

        return true;
    }

    private boolean reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, message)));
        return false;
    }
}
