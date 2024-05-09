package com.example.demo.service;

import com.example.demo.domain.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public void sendNotificationToClients(String message) {
        // 直接发送消息到所有订阅了/topic/test的客户端
        simpMessagingTemplate.convertAndSend("/topic/test", new Greeting(message));
    }
}
