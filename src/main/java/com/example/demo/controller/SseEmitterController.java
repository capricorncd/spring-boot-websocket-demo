package com.example.demo.controller;

import com.example.demo.service.SseEmitterService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * SSE(Server-sent events) controller
 */
@RestController
@RequestMapping("/sse")
public class SseEmitterController {

    @GetMapping("/channel/{channelId}")
    public SseEmitter stream(@PathVariable String channelId) {
        return SseEmitterService.connect(channelId);
    }

    @PostMapping("/disconnect/{connectionId}")
    public void disconnect(@PathVariable UUID connectionId) {
        SseEmitterService.removeConnectionId(connectionId);
    }
}
