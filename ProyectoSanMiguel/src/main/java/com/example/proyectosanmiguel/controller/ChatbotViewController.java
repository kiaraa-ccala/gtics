package com.example.proyectosanmiguel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
public class ChatbotViewController {

    @GetMapping("/chat")
    public String chatbotView() {
        return "chat/chat_new";
    }

    @GetMapping("/chatbot")
    public String mostrarChatbot(Model model, Principal principal) {
        
        return "Chatbot/chatbot";
    }
    
    @GetMapping("/chatbot/test")
    public String mostrarTestChatbot() {
        return "forward:/test_chatbot_streaming.html";
    }
}
