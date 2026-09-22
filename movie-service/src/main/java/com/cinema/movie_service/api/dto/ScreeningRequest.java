package com.cinema.movie_service.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ScreeningRequest {
    @NotNull
    private UUID movieId;
    @NotBlank
    private String room;
    @NotNull
    @Future
    private LocalDateTime showAt;
    @NotNull
    @Positive
    private Integer totalSeats;
}
