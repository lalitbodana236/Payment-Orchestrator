package com.yuno.assignment.provider.dto;

import com.yuno.assignment.enums.PaymentMethod;
import java.math.BigDecimal;

public record ProviderPaymentRequest(
        String paymentReference,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod) {
}
