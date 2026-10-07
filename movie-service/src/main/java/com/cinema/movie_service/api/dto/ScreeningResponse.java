package com.cinema.movie_service.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Representación pública de una función (screening)", example = "{\"id\": \"73d2a76f-005a-4b96-b072-cd414e5b22b6\", \"movieId\": \"d508494b-4b2a-431c-99d9-bb4fc27de754\", \"movieTitle\": \"Dune: Part Two\", \"room\": \"Sala IMAX\", \"showAt\": \"2026-10-10T20:00:00\", \"totalSeats\": 45, \"availableSeats\": 45, \"active\": true}")
@Data
@Builder
public class ScreeningResponse {

    @Schema(description = "Identificador UUID de la función", example = "73d2a76f-005a-4b96-b072-cd414e5b22b6")
    private UUID id;

    @Schema(description = "UUID de la película asociada", example = "d508494b-4b2a-431c-99d9-bb4fc27de754")
    private UUID movieId;

    @Schema(description = "Título de la película (denormalizado desde el catálogo)", example = "Dune: Part Two")
    private String movieTitle;

    @Schema(description = "Sala donde se proyecta", example = "Sala IMAX")
    private String room;

    @Schema(description = "Fecha y hora de la función", example = "2026-10-10T20:00:00")
    private LocalDateTime showAt;

    @Schema(description = "Capacidad fija de la sala", example = "45")
    private Integer totalSeats;

    @Schema(description = "Asientos disponibles (nunca negativo)", example = "45")
    private Integer availableSeats;

    @Schema(description = "Indica si la función está activa", example = "true")
    private boolean active;
}
