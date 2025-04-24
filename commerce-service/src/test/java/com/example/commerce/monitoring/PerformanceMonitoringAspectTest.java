package com.example.commerce.monitoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceMonitoringAspectTest {

    @Mock
    private MetricsService metricsService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    private PerformanceMonitoringAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new PerformanceMonitoringAspect(metricsService);
    }

    @Test
    void shouldMonitorMethodExecution() throws Throwable {
        // given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.proceed()).thenReturn("result");

        // when
        aspect.monitorPerformance(joinPoint);

        // then
        verify(metricsService).recordCartOperationTime(anyLong());
    }

    @Test
    void shouldHandleExceptionAndStillRecordMetrics() throws Throwable {
        // given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Test exception"));

        // when
        try {
            aspect.monitorPerformance(joinPoint);
        } catch (RuntimeException e) {
            // expected
        }

        // then
        verify(metricsService).recordCartOperationTime(anyLong());
    }
} 