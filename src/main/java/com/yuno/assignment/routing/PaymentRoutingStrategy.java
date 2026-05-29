package com.yuno.assignment.routing;

import com.yuno.assignment.enums.PaymentMethod;
import com.yuno.assignment.enums.PaymentProviderType;
import org.springframework.stereotype.Component;

@Component
public class PaymentRoutingStrategy implements RoutingStrategy {

    @Override
    public PaymentRoute route(PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case CARD -> new PaymentRoute(PaymentProviderType.PROVIDER_A, java.util.List.of(PaymentProviderType.PROVIDER_B));
            case UPI -> new PaymentRoute(PaymentProviderType.PROVIDER_B, java.util.List.of(PaymentProviderType.PROVIDER_A));
        };
    }
}
