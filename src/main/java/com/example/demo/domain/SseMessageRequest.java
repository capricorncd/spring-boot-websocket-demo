package com.example.demo.domain;

public record SseMessageRequest(String name) {
    public String getName() { return name; }
}