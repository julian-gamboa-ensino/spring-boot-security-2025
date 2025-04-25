package com.example.commerce.service;

import com.example.commerce.model.Cart;
import com.example.commerce.repository.CartRepository;
import com.example.commerce.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CartService
 */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private CartService cartService;

    private Cart testCart;

    @BeforeEach
    void setUp() {
        testCart = new Cart();
        testCart.setUserId(String.valueOf(1L));
        testCart.setCreatedAt(LocalDateTime.now());
        testCart.setExpiresAt(LocalDateTime.now().plusMinutes(1));
    }

    @Test
    void criarCarrinho_QuandoUsuarioSemCarrinhoAtivo_DeveCriarNovoCarrinho() {
        // Arrange
        when(cartRepository.findByUserIdAndFinalizadoFalse(anyLong()))
            .thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        Cart cart = cartService.criarCarrinho(1L);

        // Assert
        assertNotNull(cart);
        assertEquals(1L, cart.getUserId());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void criarCarrinho_QuandoUsuarioTemCarrinhoAtivo_DeveLancarExcecao() {
        // Arrange
        when(cartRepository.findByUserIdAndFinalizadoFalse(anyLong()))
            .thenReturn(Optional.of(testCart));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            cartService.criarCarrinho(1L);
        });
        verify(cartRepository, never()).save(any(Cart.class));
    }
} 