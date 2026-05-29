package com.yuno.assignment.orchestration;

import com.yuno.assignment.config.PaymentOrchestrationProperties;
import com.yuno.assignment.entity.PaymentEntity;
import com.yuno.assignment.enums.PaymentProviderType;
import com.yuno.assignment.exception.PaymentProcessingException;
import com.yuno.assignment.exception.ProviderTimeoutException;
import com.yuno.assignment.provider.connector.PaymentProviderConnector;
import com.yuno.assignment.provider.dto.ProviderPaymentRequest;
import com.yuno.assignment.provider.dto.ProviderPaymentResponse;
import com.yuno.assignment.repository.PaymentRepository;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProviderExecutor {

    private final RetryTemplate retryTemplate;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final Map<PaymentProviderType, PaymentProviderConnector> connectors;
    private final PaymentRepository paymentRepository;
    private final PaymentOrchestrationProperties properties;

    public ProviderExecutor(
            RetryTemplate providerRetryTemplate,
            CircuitBreakerRegistry circuitBreakerRegistry,
            List<PaymentProviderConnector> connectorList,
            PaymentRepository paymentRepository,
            PaymentOrchestrationProperties properties) {
        this.retryTemplate = providerRetryTemplate;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.paymentRepository = paymentRepository;
        this.properties = properties;
        this.connectors = new EnumMap<>(PaymentProviderType.class);
        connectorList.forEach(connector -> this.connectors.put(connector.provider(), connector));
    }

    public ProviderPaymentResponse execute(PaymentEntity payment, PaymentProviderType providerType) {
        PaymentProviderConnector connector = connectors.get(providerType);
        if (connector == null) {
            throw new IllegalStateException("No connector configured for provider " + providerType);
        }

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(providerType.name());
        try {
            return circuitBreaker.executeSupplier(() -> retryTemplate.execute(context -> {
                payment.setRetryCount(context.getRetryCount());
                paymentRepository.save(payment);
                return invokeConnector(connector, payment);
            }, context -> {
                Throwable throwable = context.getLastThrowable();
                if (throwable instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new PaymentProcessingException("Provider execution failed after retries", throwable);
            }));
        } catch (CallNotPermittedException exception) {
            throw new PaymentProcessingException("Circuit breaker open for provider " + providerType, exception);
        }
    }

    private ProviderPaymentResponse invokeConnector(PaymentProviderConnector connector, PaymentEntity payment) {
        ProviderPaymentRequest providerRequest = new ProviderPaymentRequest(
                payment.getPaymentReference(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod());

        CompletableFuture<ProviderPaymentResponse> future = CompletableFuture.supplyAsync(() -> connector.process(providerRequest));
        try {
            return future.get(properties.provider().timeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException exception) {
            future.cancel(true);
            throw new ProviderTimeoutException("Provider call timed out for " + connector.provider(), exception);
        } catch (ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new PaymentProcessingException("Provider call failed", cause);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new PaymentProcessingException("Provider call was interrupted", exception);
        }
    }
}
