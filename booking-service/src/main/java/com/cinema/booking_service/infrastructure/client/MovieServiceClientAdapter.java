package com.cinema.booking_service.infrastructure.client;

import com.cinema.booking_service.api.dto.ScreeningDto;
import com.cinema.booking_service.domain.MovieServiceClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MovieServiceClientAdapter implements MovieServiceClientPort {

    private final RestClient movieServiceClient;

    @Override
    public ScreeningDto getScreening(UUID screeningId) {
        return movieServiceClient.get()
                .uri("/screenings/{id}", screeningId)
                .retrieve()
                .body(ScreeningDto.class);
    }

    @Override
    @Retryable(
      retryFor = {RuntimeException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 500)
    )
    public void reserveSeat(UUID screeningId) {
        log.info("Attempting to reserve seat for screening: {}", screeningId);
        movieServiceClient.patch()
                .uri("/screenings/{id}/reserve", screeningId)
                // Note: Normally we'd pass Auth headers here, assuming simple internal calls or token relay is configured
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void releaseSeat(UUID screeningId) {
        log.info("Attempting to release seat for screening: {}", screeningId);
        try {
            movieServiceClient.patch()
                    .uri("/screenings/{id}/release", screeningId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to release seat for screening {}: {}", screeningId, e.getMessage());
            // Best effort: we don't throw exception to avoid failing the SAGA cancellation
        }
    }
}
