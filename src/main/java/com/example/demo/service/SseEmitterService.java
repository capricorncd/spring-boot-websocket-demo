package com.example.demo.service;

import com.example.demo.domain.HelloMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class SseEmitterService {
    private static final Logger logger = LoggerFactory.getLogger(SseEmitterService.class);

    private static final AtomicInteger count = new AtomicInteger(0);

    private static final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public static SseEmitter connect(String userId) {
        if (emitterMap.containsKey(userId)) {
            return emitterMap.get(userId);
        }
        SseEmitter sseEmitter = new SseEmitter(0L);
        sseEmitter.onCompletion(completionCallBack(userId));
        sseEmitter.onError(errorCallBack(userId));
        sseEmitter.onTimeout(timeoutCallBack(userId));
        emitterMap.put(userId, sseEmitter);
        count.getAndIncrement();
        return sseEmitter;
    }

    public static void sendMessage(String userId, String message) {
        if (emitterMap.containsKey(userId)) {
            try {
                emitterMap.get(userId).send(message);
            }
            catch (IOException e) {
                logger.error("User[{}] push exception: {}", userId, e.getMessage());
                removeUser(userId);
            }
        }
    }

    public static void batchSendMessage(String message, List<String> ids) {
        ids.forEach(userId -> sendMessage(message, userId));
    }

    public static void batchSendMessage(HelloMessage message) {
        emitterMap.forEach((k, v) -> {
            try {
                v.send(message, MediaType.APPLICATION_JSON);
            }
            catch (IOException e) {
                logger.error("User[{}] push exception: {}", k, e.getMessage());
                removeUser(k);
            }
        });
    }

    public static void removeUser(String userId) {
        emitterMap.remove(userId);
        count.getAndDecrement();
        logger.info("Disconnect user：{}", userId);
    }

    public static List<String> getIds() {
        return new ArrayList<>(emitterMap.keySet());
    }

    public static int getUserCount() {
        return count.intValue();
    }

    private static Runnable completionCallBack(String userId) {
        return () -> {
            logger.info("Connection completion：{}", userId);
            removeUser(userId);
        };
    }

    private static Runnable timeoutCallBack(String userId) {
        return () -> {
            logger.info("Connection timeout：{}", userId);
            removeUser(userId);
        };
    }

    private static Consumer<Throwable> errorCallBack(String userId) {
        return throwable -> {
            logger.info("Connection exception：{}", userId);
            removeUser(userId);
        };
    }
}
