package com.example.auth.repository;

import com.example.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositório de Usuários
 * ====================
 * 
 * GESTÃO DE DADOS:
 * -------------
 * 1. Estrutura da Tabela:
 *    - ID (PK, auto-incremento)
 *    - CPF (único, indexado)
 *    - Nome
 *    - Login (único, indexado)
 *    - Senha (hash)
 *    - Perfil (enum: CLIENTE, VENDEDOR)
 * 
 * 2. Dados Iniciais:
 *    - Script de inicialização no resources
 *    - Usuários pré-cadastrados
 *    - Senhas criptografadas
 * 
 * CONSULTAS PRINCIPAIS:
 * ------------------
 * - Busca por CPF
 * - Busca por login
 * - Listagem por perfil
 * - Validação de credenciais
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
} 