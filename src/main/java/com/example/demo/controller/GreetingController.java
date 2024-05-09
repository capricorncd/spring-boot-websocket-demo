package com.example.demo.controller;

import com.example.demo.domain.Greeting;
import com.example.demo.domain.HelloMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import com.example.demo.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class GreetingController {

    @Autowired
	private MessageService messageService;

    @MessageMapping("/hello")
    @SendTo("/topic/test")
    public Greeting greeting(HelloMessage message) throws Exception {
        messageService.sendNotificationToClients("我刚收到了一条消息，下面时消息内容：");
        Thread.sleep(1000); // simulated delay
        return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
    }

}


