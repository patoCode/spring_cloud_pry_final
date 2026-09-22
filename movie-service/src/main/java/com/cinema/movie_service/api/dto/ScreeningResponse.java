package com.cinema.movie_service.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ScreeningResponse {
    private UUID id;
    private UUID movieId;
    private String room;
    private LocalDateTime showAt;
    private Integer totalSeats;
    private Integer availableSeats;
    private boolean active;
}
