package com.example.commerce.service;

import com.example.commerce.model.Cart;
import com.example.commerce.repository.CartRepository;
import com.example.commerce.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Serviço responsável pela lógica de negócio relacionada a carrinhos.
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleService vehicleService;

    /**
     * Cria um novo carrinho para o usuário
     */
    @Transactional
    public Cart criarCarrinho(Long userId) {
        // Verifica se já existe um carrinho ativo
        cartRepository.findByUserIdAndFinalizadoFalse(userId)
            .ifPresent(cart -> {
                throw new RuntimeException("Usuário já possui um carrinho ativo");
            });

        Cart cart = new Cart();
        cart.setUserId(userId);
        return cartRepository.save(cart);
    }

    /**
     * Adiciona um veículo ao carrinho
     */
    @Transactional
    public void adicionarVeiculo(Long cartId, Long vehicleId) {
        Cart cart = buscarPorId(cartId);
        if (cart.isExpirado()) {
            throw new RuntimeException("Carrinho expirado");
        }
        
        cart.adicionarVeiculo(vehicleId);
        vehicleService.adicionarAoCarrinho(vehicleId, cartId);
        cartRepository.save(cart);
    }

    /**
     * Finaliza o carrinho (compra ou cancelamento)
     */
    @Transactional
    public void finalizarCarrinho(Long cartId) {
        Cart cart = buscarPorId(cartId);
        cart.finalizar();
        cartRepository.save(cart);
        
        // Remove todos os veículos do carrinho
        cart.getVehicleIds().forEach(vehicleService::removerDoCarrinho);
    }

    /**
     * Tarefa agendada para limpar carrinhos expirados
     */
    @Scheduled(fixedRate = 60000) // Executa a cada minuto
    @Transactional
    public void limparCarrinhosExpirados() {
        LocalDateTime now = LocalDateTime.now();
        cartRepository.findExpiredCarts(now)
            .forEach(cart -> this.finalizarCarrinho(cart.getId()));
    }

    private Cart buscarPorId(Long id) {
        return cartRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Carrinho não encontrado"));
    }

    public void createCart(long cartId) {
        // Implementation for creating a cart
    }
} 