package com.cinema.booking_service.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Datos para crear una reserva (entrada del SAGA)", example = "{\"screeningId\": \"73d2a76f-005a-4b96-b072-cd414e5b22b6\", \"customerId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", \"totalAmount\": 12.50}")
@Data
public class BookingRequest {

    @Schema(description = "UUID de la función a reservar", example = "73d2a76f-005a-4b96-b072-cd414e5b22b6", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private UUID screeningId;

    @Schema(description = "UUID del cliente que reserva", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private UUID customerId;

    @Schema(description = "Importe total pagado (debe ser positivo)", example = "12.50", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Positive
    private BigDecimal totalAmount;
}
