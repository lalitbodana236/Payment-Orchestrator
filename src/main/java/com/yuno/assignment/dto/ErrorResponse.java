package com.yuno.assignment.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        String correlationId,
        Instant timestamp,
        String code,
        String message,
        List<String> details) {
}
