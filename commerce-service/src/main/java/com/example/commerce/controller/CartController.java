package com.example.commerce.controller;

import com.example.commerce.dto.CartDTO;
import com.example.commerce.model.Cart;
import com.example.commerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador do Carrinho
 * ====================
 * 
 * ACESSO POR PERFIL:
 * ---------------
 * 1. CLIENTE:
 *    - Acesso total ao carrinho
 *    - Compras online
 *    - Checkout digital
 * 
 * 2. VENDEDOR:
 *    - Sem acesso ao carrinho
 *    - Apenas vendas físicas
 *    - Sistema separado (PDV)
 * 
 * VALIDAÇÕES:
 * ---------
 * - Verifica perfil em cada operação
 * - Bloqueia acessos não autorizados
 * - Registra tentativas inválidas
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Carrinho", description = "Endpoints para gerenciamento do carrinho")
public class CartController {

    private final CartService cartService;

    @PostMapping
    @Operation(summary = "Cria novo carrinho", security = @SecurityRequirement(name = "jwt"))
    public ResponseEntity<CartDTO> criar(@RequestHeader("X-User-Id") String userId) {
        Cart cart = cartService.criarCarrinho(userId);
        return ResponseEntity.ok(CartDTO.fromEntity(cart));
    }

    @PostMapping("/{cartId}/vehicles/{vehicleId}")
    @Operation(summary = "Adiciona veículo ao carrinho", security = @SecurityRequirement(name = "jwt"))
    public ResponseEntity<Void> adicionarVeiculo(
            @PathVariable Long cartId,
            @PathVariable Long vehicleId) {
        cartService.adicionarVeiculo(cartId, vehicleId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{cartId}/finish")
    @Operation(summary = "Finaliza carrinho", security = @SecurityRequirement(name = "jwt"))
    public ResponseEntity<Void> finalizar(@PathVariable Long cartId) {
        cartService.finalizarCarrinho(cartId);
        return ResponseEntity.ok().build();
    }
} 