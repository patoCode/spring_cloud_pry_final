package com.cinema.booking_service.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BookingRequest {
    @NotNull
    private UUID screeningId;
    @NotNull
    private UUID customerId;
    @NotNull
    @Positive
    private BigDecimal totalAmount;
}
