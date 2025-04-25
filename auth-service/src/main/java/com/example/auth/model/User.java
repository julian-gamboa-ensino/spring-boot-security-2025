package com.example.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidade Usuário
 * ==============
 * 
 * DADOS PRINCIPAIS:
 * --------------
 * - CPF (String, único, criptografado)
 * - Nome (String)
 * - Login (String, único)
 * - Senha (String, criptografada)
 * - Perfil (Enum: CLIENTE, VENDEDOR)
 * 
 * REGRAS DE PERFIL:
 * --------------
 * 1. CLIENTE:
 *    - Acesso exclusivo ao e-commerce
 *    - Pode realizar compras online
 *    - Visualiza apenas próprio histórico
 * 
 * 2. VENDEDOR:
 *    - Acesso ao sistema de vendas físicas
 *    - Registro de vendas presenciais
 *    - Consulta de estoque e relatórios
 * 
 * OBSERVAÇÕES:
 * ----------
 * - Usuários são pré-cadastrados (sem auto-registro)
 * - CPF deve ser válido e único
 * - Perfil é imutável após cadastro
 * - Login é case-sensitive
 */
@Entity
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String username;
    
    private String password;
    
    private String documentNumber;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();
} 