package com.cinema.booking_service.infrastructure.client;

import com.cinema.booking_service.api.dto.ScreeningDto;
import com.cinema.booking_service.domain.MovieServiceClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
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

    /**
     * Paso 3 del SAGA. Solo se reintentan fallos transitorios (red / 5xx):
     * un 422 por falta de asientos es un error de negocio y se propaga de inmediato.
     */
    @Override
    @Retryable(
      retryFor = {ResourceAccessException.class, HttpServerErrorException.class},
      maxAttemptsExpression = "${retry.max-attempts:3}",
      backoff = @Backoff(delayExpression = "${retry.backoff-delay:500}")
    )
    public void reserveSeat(UUID screeningId) {
        log.info("Attempting to reserve seat for screening: {}", screeningId);
        movieServiceClient.patch()
                .uri("/screenings/{id}/reserve", screeningId)
                // Auth: AuthTokenRelayInterceptor reenvía el Authorization del request entrante
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
