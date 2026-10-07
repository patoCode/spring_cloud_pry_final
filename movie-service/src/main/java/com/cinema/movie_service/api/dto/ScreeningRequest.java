package com.cinema.movie_service.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Datos para crear una función de cine", example = "{\"movieId\": \"d508494b-4b2a-431c-99d9-bb4fc27de754\", \"room\": \"Sala IMAX\", \"showAt\": \"2027-01-15T20:00:00\", \"totalSeats\": 45}")
@Data
public class ScreeningRequest {

    @Schema(description = "UUID de la película a proyectar", example = "d508494b-4b2a-431c-99d9-bb4fc27de754", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private UUID movieId;

    @Schema(description = "Sala donde se proyecta", example = "Sala IMAX", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String room;

    @Schema(description = "Fecha y hora de la función (debe ser futura)", example = "2027-01-15T20:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Future
    private LocalDateTime showAt;

    @Schema(description = "Capacidad fija de la sala", example = "45", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Positive
    private Integer totalSeats;
}
