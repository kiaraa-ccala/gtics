package com.example.proyectosanmiguel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatbotViewController {

    @GetMapping("/chatbot")
    public String chatbotView() {
        return "chat/chat_new";
    }
}
