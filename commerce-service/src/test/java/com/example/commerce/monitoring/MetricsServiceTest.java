package com.example.commerce.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsServiceTest {

    private MeterRegistry registry;
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metricsService = new MetricsService(registry);
    }

    @Test
    void shouldIncrementCartsCreatedCounter() {
        // when
        metricsService.incrementCartsCreated();
        metricsService.incrementCartsCreated();

        // then
        Counter counter = registry.find("carts.created").counter();
        assertEquals(2.0, counter.count());
    }

    @Test
    void shouldIncrementCartsExpiredCounter() {
        // when
        metricsService.incrementCartsExpired();

        // then
        Counter counter = registry.find("carts.expired").counter();
        assertEquals(1.0, counter.count());
    }

    @Test
    void shouldRecordCartOperationTime() {
        // when
        metricsService.recordCartOperationTime(100);
        metricsService.recordCartOperationTime(200);

        // then
        double totalTime = registry.find("cart.operation.time").timer().totalTime(java.util.concurrent.TimeUnit.MILLISECONDS);
        assertEquals(300.0, totalTime);
    }
} 