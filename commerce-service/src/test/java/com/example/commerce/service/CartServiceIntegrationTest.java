package com.example.commerce.service;

import com.example.commerce.model.*;
import com.example.commerce.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class CartServiceIntegrationTest {

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("commerce_db")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init.sql")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @BeforeAll
    static void startContainer() {
        mysqlContainer.start();
    }

    @Autowired
    private CartService cartService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CartRepository cartRepository;

    @Test
    @Transactional
    void shouldCreateCartAndAddVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setModelo("Toyota Corolla");
        vehicle.setAno(2023);
        vehicle.setPreco(new BigDecimal("125000.00"));
        vehicle.setColor(VehicleColor.PRATA);
        vehicle.setDisponivel(true);
        vehicle.setVendido(false);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        System.out.println("Saved Vehicle ID: " + savedVehicle.getId());

        Cart cart = cartService.criarCarrinho("user1");
        System.out.println("Created Cart ID: " + cart.getId());

        cartService.adicionarVeiculo(cart.getId(), vehicle.getId());

        Cart updatedCart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new AssertionError("Cart not found"));
        System.out.println("Updated Cart Vehicles: " + updatedCart.getVehicles().size());
        assertEquals(1, updatedCart.getVehicles().size());
        assertEquals("Toyota Corolla", updatedCart.getVehicles().iterator().next().getModelo());
        assertEquals(CartStatus.ACTIVE, updatedCart.getStatus());
    }
}