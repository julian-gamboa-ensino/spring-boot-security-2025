package com.example.ui.controller;

import com.example.ui.dto.LoginRequest;
import com.example.ui.dto.RegisterRequest;
import com.example.ui.service.AuthUIService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final AuthUIService authService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginRequest loginRequest,
                       BindingResult result,
                       HttpSession session) {
        if (result.hasErrors()) {
            return "login";
        }

        authService.login(loginRequest)
                .subscribe(response -> {
                    session.setAttribute("token", response.get("token"));
                    session.setAttribute("role", response.get("role"));
                });

        return "redirect:/vehicles";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest registerRequest,
                         BindingResult result,
                         HttpSession session) {
        if (result.hasErrors()) {
            return "register";
        }

        authService.register(registerRequest)
                .subscribe(response -> {
                    session.setAttribute("token", response.get("token"));
                    session.setAttribute("role", response.get("role"));
                });

        return "redirect:/vehicles";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
} 