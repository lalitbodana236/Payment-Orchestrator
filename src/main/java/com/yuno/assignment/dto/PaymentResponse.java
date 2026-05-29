package com.yuno.assignment.dto;

import com.yuno.assignment.enums.PaymentMethod;
import com.yuno.assignment.enums.PaymentProviderType;
import com.yuno.assignment.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(name = "PaymentResponse", description = "Payment status and orchestration result")
public record PaymentResponse(
        String paymentReference,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        PaymentProviderType provider,
        PaymentStatus status,
        int retryCount,
        String failureReason,
        Instant createdAt,
        Instant updatedAt) {
}
