package com.grid07.grid07.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void incrementViralityScore(Long postId, int points) {
        String key = "post:" + postId + ":virality_score";
        redisTemplate.opsForValue().increment(key, points);
    }

    public Long getViralityScore(Long postId) {
        String key = "post:" + postId + ":virality_score";
        String val = redisTemplate.opsForValue().get(key);
        return val == null ? 0L : Long.parseLong(val);
    }

    public boolean incrementBotCount(Long postId) {
        String key = "post:" + postId + ":bot_count";
        Long count = redisTemplate.opsForValue().increment(key);
        if (count > 100) {
            redisTemplate.opsForValue().decrement(key);
            return false;
        }
        return true;
    }

    public Long getBotCount(Long postId) {
        String key = "post:" + postId + ":bot_count";
        String val = redisTemplate.opsForValue().get(key);
        return val == null ? 0L : Long.parseLong(val);
    }

    public boolean isBotOnCooldown(Long botId, Long userId) {
        String key = "cooldown:bot_" + botId + ":human_" + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void setBotCooldown(Long botId, Long userId) {
        String key = "cooldown:bot_" + botId + ":human_" + userId;
        redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(10));
    }

    public boolean isNotificationOnCooldown(Long userId) {
        String key = "notif_cooldown:user_" + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void setNotificationCooldown(Long userId) {
        String key = "notif_cooldown:user_" + userId;
        redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(15));
    }

    public void pushPendingNotification(Long userId, String message) {
        String key = "user:" + userId + ":pending_notifs";
        redisTemplate.opsForList().rightPush(key, message);
    }

    public List<String> popAllPendingNotifications(Long userId) {
        String key = "user:" + userId + ":pending_notifs";
        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size == 0) return Collections.emptyList();
        return redisTemplate.opsForList().leftPop(key, size);
    }

    public Set<String> getAllPendingNotificationKeys() {
        return redisTemplate.keys("user:*:pending_notifs");
    }
}