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
    public String getCsrfInfo(javax.servlet.http.HttpServletRequest request) {
        Object csrfTokenObj = request.getAttribute("_csrf");
        if (csrfTokenObj != null) {
            // Cast to get the actual token value
            org.springframework.security.web.csrf.CsrfToken csrfToken = 
                (org.springframework.security.web.csrf.CsrfToken) csrfTokenObj;
            
            return String.format(
                "CSRF Token Details:\n" +
                "- Token Value: %s\n" +
                "- Header Name: %s\n" +
                "- Parameter Name: %s\n" +
                "- Token Class: %s",
                csrfToken.getToken(),
                csrfToken.getHeaderName(), 
                csrfToken.getParameterName(),
                csrfToken.getClass().getSimpleName()
            );
        } else {
            return "No CSRF token found in request attributes";
        }
    }
} 