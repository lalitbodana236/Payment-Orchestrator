package com.yuno.assignment.provider.connector;

import com.yuno.assignment.enums.PaymentProviderType;
import com.yuno.assignment.provider.dto.ProviderPaymentRequest;
import com.yuno.assignment.provider.dto.ProviderPaymentResponse;

public interface PaymentProviderConnector {

    PaymentProviderType provider();

    ProviderPaymentResponse process(ProviderPaymentRequest request);
}
