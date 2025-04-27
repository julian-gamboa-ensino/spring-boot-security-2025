package com.example.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Supondo que futuramente você terá estas duas dependências para injeção de serviço
    // private final UserService userService;
    // private final JwtService jwtService;

    // public AuthController(UserService userService, JwtService jwtService) {
    //     this.userService = userService;
    //     this.jwtService = jwtService;
    // }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        Map<String, Object> response = new HashMap<>();

        // --------------------------------------------
        // TODO: Substituir esta validação fixa por busca no banco de dados
        // User user = userService.findByLogin(username);
        //
        // if (user == null) {
        //     response.put("success", false);
        //     response.put("message", "Usuário não encontrado");
        //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        // }
        //
        // TODO: Validar a senha usando o UserService
        // if (!userService.validatePassword(password, user)) {
        //     response.put("success", false);
        //     response.put("message", "Senha inválida");
        //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        // }
        //
        // TODO: Gerar token JWT usando JwtService
        // String token = jwtService.generateToken(user);
        //
        // TODO: Retornar apenas os dados seguros do usuário
        // response.put("success", true);
        // response.put("token", token);
        // response.put("user", Map.of(
        //     "id", user.getId(),
        //     "login", user.getLogin(),
        //     "perfil", user.getPerfil()
        // ));
        // return ResponseEntity.ok(response);
        // --------------------------------------------

        // Código atual apenas para simulação simples (será removido na implementação real)
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
