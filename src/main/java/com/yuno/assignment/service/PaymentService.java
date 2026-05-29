package com.yuno.assignment.service;

import com.yuno.assignment.dto.PaymentRequest;
import com.yuno.assignment.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse createPayment(String idempotencyKey, PaymentRequest request);

    PaymentResponse getPayment(String paymentReference);
}
