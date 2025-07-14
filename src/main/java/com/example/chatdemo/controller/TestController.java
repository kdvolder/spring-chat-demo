package com.example.chatdemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

    // Removed conflicting mapping - static file will be served directly

    @PostMapping("/test-endpoint")
    @ResponseBody
    public String handleTestForm(@RequestParam String message) {
        return "Form submitted successfully! Message: " + message;
    }

    @GetMapping("/csrf-info")
    @ResponseBody
    public String getCsrfInfo(jakarta.servlet.http.HttpServletRequest request) {
        return "CSRF Token: " + request.getAttribute("_csrf");
    }
} 