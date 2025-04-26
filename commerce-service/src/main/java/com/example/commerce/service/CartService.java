package com.example.commerce.service;

import com.example.commerce.model.*;
import com.example.commerce.repository.CartRepository;
import com.example.commerce.repository.VehicleRepository;
import com.example.commerce.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço de Carrinho
 * =================
 * 
 * REGRAS DE NEGÓCIO:
 * ----------------
 * 1. Timeout:
 *    - 1 minuto por item no carrinho
 *    - Configurável via application.properties
 *    - Expiração invalida checkout
 * 
 * 2. Disponibilidade:
 *    - Veículo fica indisponível ao adicionar
 *    - Retorna disponível após:
 *      * Cancelamento
 *      * Timeout
 *      * Desistência
 * 
 * 3. Validações:
 *    - Obrigatório ter item no carrinho
 *    - Impede checkout com item expirado
 *    - Verifica disponibilidade
 * 
 * PROPRIEDADES CONFIGURÁVEIS:
 * ------------------------
 * cart.timeout.minutes=1
 * cart.cleanup.interval=30000
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final VehicleRepository vehicleRepository;
    private static final long CART_TIMEOUT_MINUTES = 1; // 1 minuto

    /**
     * Cria um novo carrinho para o usuário
     */
    @Transactional
    public Cart criarCarrinho(String userId) {
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
        
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new BusinessException("Veículo não encontrado"));
        cart.adicionarVeiculo(vehicle);
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
        cart.getVehicles().stream()
            .map(Vehicle::getId)
            .forEach(vehicleRepository::deleteById);
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

    public Cart getCurrentCart() {
        // Aqui você implementa a lógica para obter o carrinho atual
        // Por exemplo, usando o usuário logado ou uma sessão
        return cartRepository.findFirstByOrderByCreatedAtDesc();
    }

    public Cart createOrUpdateCart(Long vehicleId) {
        Cart cart = getCurrentCart();
        if (cart == null) {
            cart = new Cart();
            cart.setCreatedAt(LocalDateTime.now());
        }
        
        // Definir tempo de expiração
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(CART_TIMEOUT_MINUTES));
        
        // Adicionar veículo ao carrinho
        // ... resto da lógica
        
        return cartRepository.save(cart);
    }

    public boolean isCartValid(Cart cart) {
        if (cart == null || cart.getExpiresAt() == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(cart.getExpiresAt());
    }

    public boolean isVehicleInActiveCart(Long vehicleId) {
        return cartRepository.existsByVehiclesIdAndStatus(vehicleId, CartStatus.ACTIVE);
    }

    @Transactional
    public void addToCart(Long vehicleId, String userId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new BusinessException("Veículo não encontrado"));

        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
            .orElseGet(() -> createNewCart(userId));

        cart.getVehicles().add(vehicle);
        cart.setExpirationTime(LocalDateTime.now().plusMinutes(1));
        cartRepository.save(cart);
    }

    @Transactional
    public void removeFromCart(Long vehicleId, String userId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
            .orElseThrow(() -> new BusinessException("Carrinho não encontrado"));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new BusinessException("Veículo não encontrado"));

        cart.getVehicles().remove(vehicle);
        cartRepository.save(cart);
    }

    @Transactional
    public void cleanExpiredCarts() {
        List<Cart> expiredCarts = cartRepository.findByStatusAndExpirationTimeBefore(
            CartStatus.ACTIVE, 
            LocalDateTime.now()
        );

        for (Cart cart : expiredCarts) {
            cart.setStatus(CartStatus.EXPIRED);
            cart.getVehicles().clear();
            cartRepository.save(cart);
        }
    }

    private Cart createNewCart(String userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpirationTime(LocalDateTime.now().plusMinutes(1));
        return cartRepository.save(cart);
    }

    public Cart findActiveCart(String userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
            .orElseThrow(() -> new BusinessException("Carrinho não encontrado"));
    }

        /**
     * Adds a vehicle to the cart for the specified user.
     *
     * @param vehicleId the ID of the vehicle to add
     * @param userId the ID of the user
     */
    public void addVehicleToCart(Long vehicleId, String userId) {
        // Implementation logic for adding a vehicle to the cart
        // Example: Update the cart repository or perform necessary operations
    }
} 