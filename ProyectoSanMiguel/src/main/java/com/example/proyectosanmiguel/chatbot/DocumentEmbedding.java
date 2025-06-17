package com.example.proyectosanmiguel.chatbot;
import java.util.List;

public record DocumentEmbedding(String content, List<Double> embedding) {
}
