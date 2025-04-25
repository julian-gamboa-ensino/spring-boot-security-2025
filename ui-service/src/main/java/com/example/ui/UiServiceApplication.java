package com.example.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Serviço de Interface com Usuário (UI Service)
 * ==========================================
 * 
 * Responsável pela interface web do sistema, gerenciando todas as
 * interações com o usuário final.
 * 
 * Páginas Principais:
 * -----------------
 * 1. Login e Registro
 *    - Formulário de login
 *    - Cadastro de novos usuários
 * 
 * 2. Catálogo de Veículos
 *    - Listagem com filtros
 *    - Detalhes do veículo
 *    - Fotos e especificações
 * 
 * 3. Carrinho de Compras
 *    - Visualização de itens
 *    - Timer de expiração
 *    - Processo de checkout
 * 
 * Integrações:
 * -----------
 * - Auth Service: Login e segurança
 * - Commerce Service: Dados de negócio
 */
@SpringBootApplication
public class UiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UiServiceApplication.class, args);
    }
} 