package com.cinema.movie_service.api.dto;

import com.cinema.movie_service.domain.Genre;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Representación pública de una película", example = "{\"id\": \"d508494b-4b2a-431c-99d9-bb4fc27de754\", \"title\": \"Dune: Part Two\", \"director\": \"Denis Villeneuve\", \"genre\": \"SCI_FI\", \"durationMin\": 166, \"rating\": \"PG-13\", \"active\": true}")
@Data
@Builder
public class MovieResponse {

    @Schema(description = "Identificador UUID de la película", example = "d508494b-4b2a-431c-99d9-bb4fc27de754")
    private UUID id;

    @Schema(description = "Título de la película", example = "Dune: Part Two")
    private String title;

    @Schema(description = "Director de la película", example = "Denis Villeneuve")
    private String director;

    @Schema(description = "Género de la película", example = "SCI_FI")
    private Genre genre;

    @Schema(description = "Duración en minutos", example = "166")
    private Integer durationMin;

    @Schema(description = "Clasificación por edad", example = "PG-13")
    private String rating;

    @Schema(description = "Indica si la película está activa en cartelera", example = "true")
    private boolean active;

    @Schema(description = "Fecha de creación (ISO-8601)", example = "2026-10-01T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Última fecha de modificación (ISO-8601)", example = "2026-10-01T10:00:00Z")
    private Instant updatedAt;
}
