package com.lalit.paymentorchestrator.controller;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Measurement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/v1/observability")
@Tag(name = "Observability", description = "Metrics and runtime visibility endpoints")
public class ObservabilityController {

    private final MeterRegistry meterRegistry;

    public ObservabilityController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/metrics")
    @Operation(summary = "Inspect custom application metrics")
    public List<MetricSnapshot> getApplicationMetrics() {
        return meterRegistry.getMeters().stream()
                .filter(meter -> meter.getId().getName().startsWith("payment."))
                .map(MetricSnapshot::from)
                .toList();
    }

    public record MetricSnapshot(
            String name,
            String type,
            String description,
            List<TagValue> tags,
            List<MeasurementValue> measurements) {

        static MetricSnapshot from(Meter meter) {
            return new MetricSnapshot(
                    meter.getId().getName(),
                    meter.getId().getType().name(),
                    meter.getId().getDescription(),
                    meter.getId().getTags().stream()
                            .map(tag -> new TagValue(tag.getKey(), tag.getValue()))
                            .toList(),
                    StreamSupport.stream(meter.measure().spliterator(), false)
                            .map(MeasurementValue::from)
                            .toList());
        }
    }

    public record TagValue(String key, String value) {
    }

    public record MeasurementValue(String statistic, double value) {
        static MeasurementValue from(Measurement measurement) {
            return new MeasurementValue(measurement.getStatistic().name(), measurement.getValue());
        }
    }
}
