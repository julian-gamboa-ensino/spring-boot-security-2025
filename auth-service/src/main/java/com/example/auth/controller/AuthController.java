package com.example.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @GetMapping("/health")
    public String health() {
        return "Auth Service is running!";
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestParam String username, @RequestParam String password) {
        Map<String, String> response = new HashMap<>();
        
        // Simples validação de exemplo
        if ("admin".equals(username) && "password".equals(password)) {
            response.put("status", "success");
            response.put("token", "dummy-token-123");
        } else {
            response.put("status", "error");
            response.put("message", "Invalid credentials");
        }
        
        return response;
    }
} 