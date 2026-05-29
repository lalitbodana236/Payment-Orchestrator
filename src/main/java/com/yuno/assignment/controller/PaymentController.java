package com.yuno.assignment.controller;

import com.yuno.assignment.dto.ApiResponse;
import com.yuno.assignment.dto.PaymentRequest;
import com.yuno.assignment.dto.PaymentResponse;
import com.yuno.assignment.service.PaymentService;
import com.yuno.assignment.util.CorrelationIdHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payment orchestration APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a payment")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Payment accepted for orchestration")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Validation failed",
            content = @Content(examples = @ExampleObject(value = "{\"code\":\"VALIDATION_ERROR\"}")))
    public ApiResponse<PaymentResponse> createPayment(
            @Parameter(description = "Idempotency key used to deduplicate create requests", required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {
        return new ApiResponse<>(
                CorrelationIdHolder.get().orElse("N/A"),
                Instant.now(),
                paymentService.createPayment(idempotencyKey, request));
    }

    @GetMapping("/{paymentReference}")
    @Operation(summary = "Fetch payment by payment reference")
    public ApiResponse<PaymentResponse> getPayment(@PathVariable String paymentReference) {
        return new ApiResponse<>(
                CorrelationIdHolder.get().orElse("N/A"),
                Instant.now(),
                paymentService.getPayment(paymentReference));
    }
}
