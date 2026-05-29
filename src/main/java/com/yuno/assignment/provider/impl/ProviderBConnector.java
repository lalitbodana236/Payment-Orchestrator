package com.yuno.assignment.provider.impl;

import com.yuno.assignment.enums.PaymentProviderType;
import com.yuno.assignment.enums.PaymentStatus;
import com.yuno.assignment.provider.connector.PaymentProviderConnector;
import com.yuno.assignment.provider.dto.ProviderPaymentRequest;
import com.yuno.assignment.provider.dto.ProviderPaymentResponse;
import org.springframework.stereotype.Component;

@Component
public class ProviderBConnector implements PaymentProviderConnector {

    @Override
    public PaymentProviderType provider() {
        return PaymentProviderType.PROVIDER_B;
    }

    @Override
    public ProviderPaymentResponse process(ProviderPaymentRequest request) {
        return new ProviderPaymentResponse(
                provider(),
                PaymentStatus.SUCCESS,
                "pb_" + request.paymentReference());
    }
}
