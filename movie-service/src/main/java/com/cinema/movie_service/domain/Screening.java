package com.cinema.movie_service.domain;

import com.cinema.movie_service.exception.NoSeatsAvailableException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Screening {
    private UUID id;
    private UUID movieId;
    private String room;
    private LocalDateTime showAt;
    private Integer totalSeats;
    private Integer availableSeats;
    @Builder.Default
    private boolean active = true;

    public void reserve() {
        if (this.availableSeats == null || this.availableSeats <= 0) {
            throw new NoSeatsAvailableException("Función sin asientos disponibles");
        }
        this.availableSeats--;
    }

    public void release() {
        if (this.availableSeats != null && this.availableSeats < this.totalSeats) {
            this.availableSeats++;
        }
    }
}
