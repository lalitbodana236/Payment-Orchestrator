package com.yuno.assignment.routing;

import com.yuno.assignment.enums.PaymentProviderType;
import java.util.List;

public record PaymentRoute(
        PaymentProviderType primaryProvider,
        List<PaymentProviderType> failoverProviders) {

    public List<PaymentProviderType> candidates() {
        return failoverProviders.isEmpty()
                ? List.of(primaryProvider)
                : java.util.stream.Stream.concat(
                                java.util.stream.Stream.of(primaryProvider),
                                failoverProviders.stream())
                        .toList();
    }
}
