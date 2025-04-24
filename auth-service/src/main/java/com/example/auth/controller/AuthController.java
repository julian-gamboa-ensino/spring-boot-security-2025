package com.example.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        Map<String, Object> response = new HashMap<>();
        
        if ("admin".equals(username) && "admin123".equals(password)) {
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", "dummy-token-" + System.currentTimeMillis());
            return ResponseEntity.ok(response);
        }
        
        response.put("success", false);
        response.put("message", "Invalid credentials");
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("Auth service is working!");
    }
} 