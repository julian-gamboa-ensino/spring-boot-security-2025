package com.example.ui.controller;

import com.example.ui.service.UserUIService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserViewController {

    private final UserUIService userService;

    @GetMapping
    public String listUsers(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        String role = (String) session.getAttribute("role");

        if (!"VENDEDOR".equals(role)) {
            return "redirect:/vehicles";
        }

        userService.getAllUsers(token)
                .subscribe(users -> model.addAttribute("users", users));

        return "user/list";
    }
} 