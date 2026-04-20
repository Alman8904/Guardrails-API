package com.grid07.grid07.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class NotificationScheduler {

    @Autowired
    private RedisService redisService;

    @Scheduled(fixedDelay = 300000)
    public void sweepPendingNotifications() {
        Set<String> keys = redisService.getAllPendingNotificationKeys();
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            String userIdStr = key.split(":")[1];
            Long userId = Long.parseLong(userIdStr);

            List<String> notifications = redisService.popAllPendingNotifications(userId);
            if (notifications.isEmpty()) continue;

            String first = notifications.get(0);
            int others = notifications.size() - 1;

            if (others > 0) {
                System.out.println("Summarized Push Notification: " + first + " and " + others + " others interacted with your posts.");
            } else {
                System.out.println("Summarized Push Notification: " + first);
            }
        }
    }
}