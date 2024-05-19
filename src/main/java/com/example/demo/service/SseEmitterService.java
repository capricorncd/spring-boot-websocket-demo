package com.example.demo.service;

import com.example.demo.domain.SseMessageResponse;
import com.example.demo.domain.SseEmitterRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * SSE(Server-sent events) service
 */
public class SseEmitterService {
    private static final Logger logger = LoggerFactory.getLogger(SseEmitterService.class);

    private static final AtomicInteger count = new AtomicInteger(0);

    private static final Map<UUID, SseEmitterRecord> emitterMap = new ConcurrentHashMap<>();

    public static SseEmitter connect(String channelId) {
        UUID id = UUID.randomUUID();
        SseEmitter sseEmitter = new SseEmitter(0L);
        sseEmitter.onCompletion(completionCallBack(id));
        sseEmitter.onError(errorCallBack(id));
        sseEmitter.onTimeout(timeoutCallBack(id));
        emitterMap.put(id, new SseEmitterRecord(channelId, sseEmitter));
        count.getAndIncrement();
        sendMessage(id, new SseMessageResponse(
                id.toString(),
                channelId,
                SseMessageResponse.Types.CONNECTION,
                SseMessageResponse.Actions.CONNECT,
                null));
        return sseEmitter;
    }

    public static void sendMessage(UUID id, SseMessageResponse message) {
        if (emitterMap.containsKey(id)) {
            try {
                emitterMap.get(id).emitter().send(message, MediaType.APPLICATION_JSON);
            }
            catch (IOException e) {
                logger.error("Channel[{}] push exception: {}", id, e.getMessage());
                removeConnectionId(id);
            }
        }
    }

    public static void batchSendMessage(SseMessageResponse message, List<UUID> ids) {
        ids.forEach(id -> sendMessage(id, message));
    }

    public static void batchSendMessage(String channelId, SseMessageResponse.Actions action, Object detail) {
        emitterMap.forEach((k, v) -> {
            if (Objects.equals(v.channelId(), channelId)) {
                try {
                    v.emitter().send(SseMessageResponse.create(k.toString(), channelId, action, detail), MediaType.APPLICATION_JSON);
                }
                catch (IOException e) {
                    logger.error("Channel[{}] push exception: {}", k, e.getMessage());
                    removeConnectionId(k);
                }
            }
        });
    }

    public static void removeConnectionId(UUID id) {
        emitterMap.remove(id);
        count.getAndDecrement();
        logger.info("Disconnect Channel：{}", id);
    }

    public static List<UUID> getIds() {
        return new ArrayList<>(emitterMap.keySet());
    }

    public static int getChannelCount() {
        return count.intValue();
    }

    private static Runnable completionCallBack(UUID id) {
        return () -> {
            logger.info("Connection completion：{}", id);
            removeConnectionId(id);
        };
    }

    private static Runnable timeoutCallBack(UUID id) {
        return () -> {
            logger.info("Connection timeout：{}", id);
            removeConnectionId(id);
        };
    }

    private static Consumer<Throwable> errorCallBack(UUID id) {
        return throwable -> {
            logger.info("Connection exception：{}", id);
            removeConnectionId(id);
        };
    }
}
