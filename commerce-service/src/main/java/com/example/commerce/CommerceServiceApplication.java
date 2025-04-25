package com.example.commerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

/**
 * Serviço de Comércio (Commerce Service)
 * ====================================
 * 
 * Serviço central do sistema, responsável por toda a lógica de negócio
 * relacionada a veículos, vendas e carrinhos de compra.
 * 
 * Funcionalidades Principais:
 * ------------------------
 * 1. Gestão de Veículos
 *    - Cadastro e atualização
 *    - Controle de disponibilidade
 *    - Precificação
 * 
 * 2. Carrinho de Compras
 *    - Reserva temporária (1 minuto)
 *    - Validação de disponibilidade
 *    - Processo de checkout
 * 
 * 3. Vendas
 *    - Registro de transações
 *    - Histórico de vendas
 *    - Relatórios
 * 
 * Integrações:
 * -----------
 * - Auth Service: Validação de tokens e permissões
 * - UI Service: Interface com usuário
 * 
 * A anotação @EnableScheduling é usada para habilitar o agendamento de tarefas,
 * necessário para a limpeza automática de carrinhos expirados (timeout de 1 minuto).
 */
@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(
    info = @Info(
        title = "API de Comércio",
        version = "1.0",
        description = "API para gerenciamento de veículos, carrinho e vendas do sistema de e-commerce"
    )
)
public class CommerceServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CommerceServiceApplication.class, args);
    }
} 