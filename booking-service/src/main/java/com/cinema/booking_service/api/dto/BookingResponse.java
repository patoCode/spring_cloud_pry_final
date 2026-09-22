package com.cinema.booking_service.api.dto;

import com.cinema.booking_service.domain.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class BookingResponse {
    private UUID id;
    private UUID screeningId;
    private String movieTitle;
    private UUID customerId;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant updatedAt;
}
