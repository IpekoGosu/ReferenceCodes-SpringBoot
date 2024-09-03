package com.example.demo.global.redis;

import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * @param key   String
     * @param value String
     * @param ttl   단위 - milliseconds
     */
    public void set(String key, String value, long ttl) {
        redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.MILLISECONDS);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    public void delete(String key) {
        redisTemplate.unlink(key);
    }
    
    public boolean hasKey(String key) {
        // null값 문제 방지
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public long getTtl(String key) {
        Long ttl = redisTemplate.getExpire(key);
        if (ttl == null) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "null ttl value");
        }
        return ttl;
    }

    public Set<String> getKeys(String keyPattern) {
        return redisTemplate.keys(keyPattern);
    }

}
