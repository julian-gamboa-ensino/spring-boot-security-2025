package com.example.commerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

/**
 * Classe principal do serviço de comércio.
 * Este serviço é responsável por:
 * 1. Gerenciamento de veículos (listagem, detalhamento)
 * 2. Controle do carrinho de compras
 * 3. Controle de estoque
 * 4. Processamento de vendas
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