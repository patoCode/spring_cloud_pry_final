package com.cinema.booking_service.api.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ScreeningDto {
    private UUID id;
    private UUID movieId;
    private Integer availableSeats;
}
