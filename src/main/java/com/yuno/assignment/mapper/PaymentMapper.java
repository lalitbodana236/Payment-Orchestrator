package com.yuno.assignment.mapper;

import com.yuno.assignment.dto.PaymentResponse;
import com.yuno.assignment.entity.PaymentEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(PaymentEntity payment) {
        return new PaymentResponse(
                payment.getPaymentReference(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getProvider(),
                payment.getStatus(),
                payment.getRetryCount(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt());
    }
}
