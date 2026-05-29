package com.yuno.assignment.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "payment.orchestration")
public record PaymentOrchestrationProperties(
        @NotNull Retry retry,
        @NotNull Idempotency idempotency,
        @NotNull Provider provider) {

    public record Retry(
            @Min(1) int maxAttempts,
            @Min(1) long initialIntervalMs,
            double multiplier,
            @Min(1) long maxIntervalMs) {
    }

    public record Idempotency(
            @Min(1) long ttlHours,
            @Min(1) long lockTtlSeconds,
            @Min(1) long waitTimeoutMs,
            @Min(10) long pollIntervalMs) {
    }

    public record Provider(@Min(100) long timeoutMs) {
        public Duration timeout() {
            return Duration.ofMillis(timeoutMs);
        }
    }
}
