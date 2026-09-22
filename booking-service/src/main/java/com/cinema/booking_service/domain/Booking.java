package com.cinema.booking_service.domain;

import com.cinema.booking_service.exception.InvalidBookingStateException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    private UUID id;
    private UUID screeningId;
    private String movieTitle;
    private UUID customerId;
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant updatedAt;

    public void cancel() {
        if (this.status == BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException("Reserva ya se encuentra cancelada");
        }
        this.status = BookingStatus.CANCELLED;
    }
    
    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }
}
