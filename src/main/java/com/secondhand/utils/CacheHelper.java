package com.secondhand.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Redis 缓存清理工具：DEL 不支持通配符，必须先用 KEYS/SCAN 拿到 key 再删。
 */
@Component
public class CacheHelper {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void evictByPrefix(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
