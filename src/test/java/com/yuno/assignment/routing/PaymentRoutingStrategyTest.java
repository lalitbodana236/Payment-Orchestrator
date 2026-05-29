package com.yuno.assignment.routing;

import com.yuno.assignment.enums.PaymentMethod;
import com.yuno.assignment.enums.PaymentProviderType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentRoutingStrategyTest {

    private final PaymentRoutingStrategy strategy = new PaymentRoutingStrategy();

    @Test
    void routesCardPaymentsToProviderAFirst() {
        PaymentRoute route = strategy.route(PaymentMethod.CARD);

        assertThat(route.primaryProvider()).isEqualTo(PaymentProviderType.PROVIDER_A);
        assertThat(route.candidates()).containsExactly(
                PaymentProviderType.PROVIDER_A,
                PaymentProviderType.PROVIDER_B);
    }

    @Test
    void routesUpiPaymentsToProviderBFirst() {
        PaymentRoute route = strategy.route(PaymentMethod.UPI);

        assertThat(route.primaryProvider()).isEqualTo(PaymentProviderType.PROVIDER_B);
        assertThat(route.candidates()).containsExactly(
                PaymentProviderType.PROVIDER_B,
                PaymentProviderType.PROVIDER_A);
    }
}
