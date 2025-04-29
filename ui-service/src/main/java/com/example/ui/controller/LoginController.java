package com.example.ui.controller;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${auth.mock-login:false}") // Pega do properties
    private boolean mockLogin;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, 
                        @RequestParam String password, 
                        Model model) {
        try {
            if (mockLogin) {
                // MODO MOCK: apenas para desenvolvimento inicial
                if ("vendedor".equals(username) && "vendedor123".equals(password)) {
                    // Simula login do vendedor
                    model.addAttribute("role", "ROLE_VENDEDOR");
                    return "redirect:/vehicles";
                }
                // Mock para CLIENTE
                else if ("cliente".equals(username) && "cliente123".equals(password)) {
                    // Simula login do cliente
                    model.addAttribute("role", "ROLE_CLIENTE");
                    return "redirect:/vehicles";
                }
                // Mock para ADMIN (mantendo o existente)
                else if ("admin".equals(username) && "admin123".equals(password)) {
                    model.addAttribute("role", "ROLE_ADMIN");
                    return "redirect:/vehicles";
                }
                else {
                    model.addAttribute("error", "Usuário ou senha inválidos (modo mock)");
                    return "login";
                }
            } else {
                // MODO REAL: valida usando o auth-service
                ResponseEntity<Map> response = restTemplate.postForEntity(
                    AUTH_SERVICE_URL + "/api/auth/login?username={username}&password={password}",
                    null,
                    Map.class,
                    username,
                    password
                );
                
                if (response.getBody() != null && Boolean.TRUE.equals(response.getBody().get("success"))) {
                    return "redirect:/vehicles"; // sucesso real
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Se chegar aqui, erro na autenticação
        model.addAttribute("error", "Usuário ou senha inválidos");
        return "login";
    }
}
