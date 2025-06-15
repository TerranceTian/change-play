package com.h5game.threebody.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.h5game.threebody.model.ChatMessage;

@Controller
public class ChatController {

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage handleMessage(ChatMessage message) {
        return message;
    }

    @MessageMapping("/join")
    @SendTo("/topic/messages")
    public ChatMessage handleJoin(String username) {
        return new ChatMessage("系统", username + " 加入了聊天室", true);
    }
}
