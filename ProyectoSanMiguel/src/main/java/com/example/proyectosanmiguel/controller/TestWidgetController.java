package com.example.proyectosanmiguel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestWidgetController {

    @GetMapping("/test-widget")
    public String testWidget() {
        return "test_widget";
    }
}
