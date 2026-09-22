package com.cinema.booking_service.domain;

import com.cinema.booking_service.api.dto.ScreeningDto;

import java.util.UUID;

public interface MovieServiceClientPort {
    ScreeningDto getScreening(UUID screeningId);
    void reserveSeat(UUID screeningId);
    void releaseSeat(UUID screeningId);
}
