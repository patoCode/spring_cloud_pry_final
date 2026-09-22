package com.cinema.movie_service.api.dto;

import com.cinema.movie_service.domain.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MovieRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String director;
    @NotNull
    private Genre genre;
    @Positive
    private Integer durationMin;
    private String rating;
}
