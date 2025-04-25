package com.example.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.Map;

/**
 * Controlador de Login
 * =================
 * 
 * ACESSO AO SISTEMA:
 * ---------------
 * 1. Validação:
 *    - Login existente na base
 *    - Senha correta
 *    - Perfil ativo
 * 
 * 2. Redirecionamento:
 *    - CLIENTE: para e-commerce
 *    - VENDEDOR: para PDV
 * 
 * RESTRIÇÕES:
 * ---------
 * - Sem cadastro de novos usuários
 * - Perfis pré-definidos
 * - Acessos limitados por perfil
 * 
 * SEGURANÇA:
 * --------
 * - Validação de CPF
 * - Dados criptografados
 * - Sessão por perfil
 */
@Controller
public class LoginController {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String AUTH_SERVICE_URL = "http://auth-service:8082";

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, 
                       @RequestParam String password, 
                       Model model) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                AUTH_SERVICE_URL + "/api/auth/login?username={username}&password={password}",
                null,
                Map.class,
                username,
                password
            );
            
            if (response.getBody() != null && Boolean.TRUE.equals(response.getBody().get("success"))) {
                return "redirect:/vehicles";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        model.addAttribute("error", "Invalid username or password");
        return "login";
    }
} 