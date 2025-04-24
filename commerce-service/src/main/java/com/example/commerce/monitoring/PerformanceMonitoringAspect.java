package com.example.commerce.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspecto que monitora o tempo de execução dos métodos.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class PerformanceMonitoringAspect {

    private final MetricsService metricsService;

    @Around("@annotation(Monitored)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String methodName = joinPoint.getSignature().getName();
            
            log.info("Method {} executed in {}ms", methodName, duration);
            metricsService.recordCartOperationTime(duration);
        }
    }
} 