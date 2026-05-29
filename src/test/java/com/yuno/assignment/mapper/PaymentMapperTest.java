package com.yuno.assignment.mapper;

import com.yuno.assignment.entity.PaymentEntity;
import com.yuno.assignment.enums.PaymentMethod;
import com.yuno.assignment.enums.PaymentProviderType;
import com.yuno.assignment.enums.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMapperTest {

    private final PaymentMapper paymentMapper = new PaymentMapper();

    @Test
    void mapsEntityToResponse() {
        PaymentEntity payment = PaymentEntity.builder()
                .paymentReference("pay_123")
                .amount(new BigDecimal("12.50"))
                .currency("USD")
                .paymentMethod(PaymentMethod.CARD)
                .provider(PaymentProviderType.PROVIDER_A)
                .status(PaymentStatus.SUCCESS)
                .retryCount(1)
                .failureReason(null)
                .build();

        assertThat(paymentMapper.toResponse(payment))
                .extracting(
                        "paymentReference",
                        "amount",
                        "currency",
                        "paymentMethod",
                        "provider",
                        "status",
                        "retryCount")
                .containsExactly(
                        "pay_123",
                        new BigDecimal("12.50"),
                        "USD",
                        PaymentMethod.CARD,
                        PaymentProviderType.PROVIDER_A,
                        PaymentStatus.SUCCESS,
                        1);
    }
}
