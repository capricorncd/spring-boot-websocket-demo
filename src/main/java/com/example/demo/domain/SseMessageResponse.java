package com.example.demo.domain;

public record SseMessageResponse(String id, String userId, Types type, Actions action, Object detail) {
    public static enum Types {
        CONNECTION,
        MESSAGE;
    }
    public static enum Actions {
        CONNECT,
        CREATE,
        UPDATE,
        DELETE;
    }
    public static SseMessageResponse create(String id, String userId, Actions action, Object detail) {
        return new SseMessageResponse(id, userId, Types.MESSAGE, action, detail);
    }
}
