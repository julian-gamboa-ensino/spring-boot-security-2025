package com.example.commerce.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Serviço responsável por coletar e registrar métricas da aplicação.
 */
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry registry;

    private final Counter cartsCreatedCounter;
    private final Counter cartsExpiredCounter;
    private final Counter salesCompletedCounter;
    private final Timer cartOperationTimer;


    @Autowired
    public MetricsService(MeterRegistry registry) {
        this.registry = registry;
        this.cartsCreatedCounter = Counter.builder("carts.created")
                .description("Number of shopping carts created")
                .register(registry);
        this.cartsExpiredCounter = Counter.builder("carts.expired")
                .description("Number of shopping carts expired")
                .register(registry);
        this.salesCompletedCounter = Counter.builder("sales.completed")
                .description("Number of sales completed")
                .register(registry);
        this.cartOperationTimer = Timer.builder("cart.operation.time")
                .description("Time spent on cart operations")
                .register(registry);
    }

    public void incrementCartsCreated() {
        cartsCreatedCounter.increment();
    }

    public void incrementCartsExpired() {
        cartsExpiredCounter.increment();
    }

    public void incrementSalesCompleted() {
        salesCompletedCounter.increment();
    }

    public void recordCartOperationTime(long timeInMs) {
        cartOperationTimer.record(timeInMs, TimeUnit.MILLISECONDS);
    }
} 