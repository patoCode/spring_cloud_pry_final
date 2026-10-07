package com.cinema.movie_service.api.dto;

import com.cinema.movie_service.domain.Genre;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Schema(description = "Datos para crear o actualizar una película", example = "{\"title\": \"Dune: Part Two\", \"director\": \"Denis Villeneuve\", \"genre\": \"SCI_FI\", \"durationMin\": 166, \"rating\": \"PG-13\"}")
@Data
public class MovieRequest {

    @Schema(description = "Título de la película", example = "Dune: Part Two", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String title;

    @Schema(description = "Director de la película", example = "Denis Villeneuve", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String director;

    @Schema(description = "Género de la película", example = "SCI_FI", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Genre genre;

    @Schema(description = "Duración en minutos", example = "166")
    @Positive
    private Integer durationMin;

    @Schema(description = "Clasificación por edad", example = "PG-13")
    private String rating;
}
