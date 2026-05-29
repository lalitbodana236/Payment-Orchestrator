package com.yuno.assignment.dto;

import com.yuno.assignment.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

@Schema(name = "PaymentRequest", description = "Request payload for creating a payment")
public record PaymentRequest(
        @NotNull
        @DecimalMin(value = "0.01")
        @Digits(integer = 15, fraction = 4)
        BigDecimal amount,
        @NotBlank
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a valid ISO-4217 uppercase code")
        String currency,
        @NotNull
        PaymentMethod paymentMethod) {
}
