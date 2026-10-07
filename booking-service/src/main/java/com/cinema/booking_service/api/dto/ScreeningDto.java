package com.cinema.booking_service.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Schema(description = "Proyección mínima de una función que consume booking-service desde movie-service", example = "{\"id\": \"73d2a76f-005a-4b96-b072-cd414e5b22b6\", \"movieId\": \"d508494b-4b2a-431c-99d9-bb4fc27de754\", \"movieTitle\": \"Dune: Part Two\", \"availableSeats\": 45}")
@Data
public class ScreeningDto {

    @Schema(description = "UUID de la función", example = "73d2a76f-005a-4b96-b072-cd414e5b22b6")
    private UUID id;

    @Schema(description = "UUID de la película asociada", example = "d508494b-4b2a-431c-99d9-bb4fc27de754")
    private UUID movieId;

    @Schema(description = "Título de la película (origen del snapshot de la reserva)", example = "Dune: Part Two")
    private String movieTitle;

    @Schema(description = "Asientos disponibles en la función", example = "45")
    private Integer availableSeats;
}
