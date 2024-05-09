package com.example.demo.domain;

public record HelloMessage(String name) {
    public String getName() { return name; }
}
