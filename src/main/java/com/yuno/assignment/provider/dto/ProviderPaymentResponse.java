package com.yuno.assignment.provider.dto;

import com.yuno.assignment.enums.PaymentProviderType;
import com.yuno.assignment.enums.PaymentStatus;

public record ProviderPaymentResponse(
        PaymentProviderType provider,
        PaymentStatus status,
        String providerReference) {
}
