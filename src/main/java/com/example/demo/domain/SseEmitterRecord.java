package com.example.demo.domain;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public record SseEmitterRecord(String channelId, SseEmitter emitter) {
}
