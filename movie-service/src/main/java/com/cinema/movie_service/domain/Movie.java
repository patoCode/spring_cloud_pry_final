package com.cinema.movie_service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    private UUID id;
    private String title;
    private String director;
    private Genre genre;
    private Integer durationMin;
    private String rating;
    @Builder.Default
    private boolean active = true;
    private Instant createdAt;
    private Instant updatedAt;
}
