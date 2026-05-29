package com.yuno.assignment.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuno.assignment.dto.PaymentRequest;
import com.yuno.assignment.enums.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class HashingUtilsTest {

    private final HashingUtils hashingUtils = new HashingUtils(new ObjectMapper());

    @Test
    void samePayloadProducesSameHash() {
        PaymentRequest request = new PaymentRequest(new BigDecimal("10.00"), "USD", PaymentMethod.CARD);

        String first = hashingUtils.sha256(request);
        String second = hashingUtils.sha256(request);

        assertThat(first).isEqualTo(second);
    }

    @Test
    void differentPayloadProducesDifferentHash() {
        PaymentRequest firstRequest = new PaymentRequest(new BigDecimal("10.00"), "USD", PaymentMethod.CARD);
        PaymentRequest secondRequest = new PaymentRequest(new BigDecimal("11.00"), "USD", PaymentMethod.CARD);

        assertThat(hashingUtils.sha256(firstRequest))
                .isNotEqualTo(hashingUtils.sha256(secondRequest));
    }
}
