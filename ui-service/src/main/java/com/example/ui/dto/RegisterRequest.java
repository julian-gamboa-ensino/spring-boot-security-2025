package com.example.ui.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Login é obrigatório")
    private String login;
    
    @NotBlank(message = "Senha é obrigatória")
    private String password;
    
    @NotBlank(message = "Nome é obrigatório")
    private String name;
    
    @NotBlank(message = "CPF é obrigatório")
    private String cpf;
    
    private String role;
} 