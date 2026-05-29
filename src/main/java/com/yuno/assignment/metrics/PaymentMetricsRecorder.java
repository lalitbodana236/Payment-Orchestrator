package com.yuno.assignment.metrics;

import com.yuno.assignment.enums.PaymentProviderType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentMetricsRecorder {

    private final MeterRegistry meterRegistry;

    public Timer.Sample startSample() {
        return Timer.start(meterRegistry);
    }

    public void recordApiLatency(Timer.Sample sample, String operation, String outcome) {
        sample.stop(Timer.builder("payment.api.latency")
                .tag("operation", operation)
                .tag("outcome", outcome)
                .register(meterRegistry));
    }

    public void incrementPaymentOutcome(String outcome, PaymentProviderType providerType) {
        Counter.builder("payment.processed.total")
                .tag("outcome", outcome)
                .tag("provider", providerType == null ? "UNKNOWN" : providerType.name())
                .register(meterRegistry)
                .increment();
    }

    public void incrementRetry(String provider, String reason) {
        Counter.builder("payment.retry.total")
                .tag("provider", provider)
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }
}
