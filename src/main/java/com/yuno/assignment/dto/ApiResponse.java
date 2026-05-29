package com.yuno.assignment.dto;

import java.time.Instant;

public record ApiResponse<T>(
        String correlationId,
        Instant timestamp,
        T data) {
}
