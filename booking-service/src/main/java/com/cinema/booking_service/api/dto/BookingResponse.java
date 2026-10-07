package com.cinema.booking_service.api.dto;

import com.cinema.booking_service.domain.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Representación pública de una reserva", example = "{\"id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"screeningId\": \"73d2a76f-005a-4b96-b072-cd414e5b22b6\", \"movieTitle\": \"Dune: Part Two\", \"customerId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"status\": \"CONFIRMED\", \"totalAmount\": 12.50}")
@Data
@Builder
public class BookingResponse {

    @Schema(description = "Identificador UUID de la reserva", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "UUID de la función reservada", example = "73d2a76f-005a-4b96-b072-cd414e5b22b6")
    private UUID screeningId;

    @Schema(description = "Snapshot del título de la película al momento de reservar (no cambia si cambia el catálogo)", example = "Dune: Part Two")
    private String movieTitle;

    @Schema(description = "UUID del cliente que reserva", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID customerId;

    @Schema(description = "Estado de la reserva", example = "CONFIRMED")
    private BookingStatus status;

    @Schema(description = "Importe total pagado", example = "12.50")
    private BigDecimal totalAmount;

    @Schema(description = "Fecha de creación (ISO-8601)", example = "2026-10-01T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Última fecha de modificación (ISO-8601)", example = "2026-10-01T10:00:00Z")
    private Instant updatedAt;
}
