package com.example.demo.controller;

import com.example.demo.service.SseEmitterService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
public class SseEmitterController {

    @GetMapping("/user/{userId}")
    public SseEmitter stream(@PathVariable String userId) {
        return SseEmitterService.connect(userId);
    }

    @PostMapping("/disconnect/{userId}")
    public void disconnect(@PathVariable String userId) {
        SseEmitterService.removeUser(userId);
    }
}
