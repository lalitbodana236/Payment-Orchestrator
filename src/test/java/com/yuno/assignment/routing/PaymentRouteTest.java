package com.yuno.assignment.routing;

import com.yuno.assignment.enums.PaymentProviderType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentRouteTest {

    @Test
    void candidatesReturnsPrimaryWhenNoFailoversExist() {
        PaymentRoute route = new PaymentRoute(PaymentProviderType.PROVIDER_A, List.of());

        assertThat(route.candidates()).containsExactly(PaymentProviderType.PROVIDER_A);
    }

    @Test
    void candidatesReturnsPrimaryBeforeFailovers() {
        PaymentRoute route = new PaymentRoute(
                PaymentProviderType.PROVIDER_A,
                List.of(PaymentProviderType.PROVIDER_B));

        assertThat(route.candidates()).containsExactly(
                PaymentProviderType.PROVIDER_A,
                PaymentProviderType.PROVIDER_B);
    }
}
