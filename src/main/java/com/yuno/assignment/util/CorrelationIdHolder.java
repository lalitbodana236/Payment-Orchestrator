package com.yuno.assignment.util;

import java.util.Optional;
import org.slf4j.MDC;

public final class CorrelationIdHolder {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    private CorrelationIdHolder() {
    }

    public static Optional<String> get() {
        return Optional.ofNullable(MDC.get(MDC_KEY));
    }
}
