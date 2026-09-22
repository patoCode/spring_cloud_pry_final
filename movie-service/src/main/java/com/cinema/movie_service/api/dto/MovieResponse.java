package com.cinema.movie_service.api.dto;

import com.cinema.movie_service.domain.Genre;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class MovieResponse {
    private UUID id;
    private String title;
    private String director;
    private Genre genre;
    private Integer durationMin;
    private String rating;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
