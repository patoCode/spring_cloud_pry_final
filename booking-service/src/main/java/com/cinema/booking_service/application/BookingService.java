package com.cinema.booking_service.application;

import com.cinema.booking_service.api.dto.ScreeningDto;
import com.cinema.booking_service.domain.Booking;
import com.cinema.booking_service.domain.BookingRepositoryPort;
import com.cinema.booking_service.domain.BookingStatus;
import com.cinema.booking_service.domain.MovieServiceClientPort;
import com.cinema.booking_service.exception.InvalidBookingStateException;
import com.cinema.booking_service.exception.ResourceNotFoundException;
import com.cinema.booking_service.exception.SagaFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepositoryPort repositoryPort;
    private final MovieServiceClientPort movieClient;

    /**
     * SAGA de reserva (4 pasos).
     *
     * No lleva @Transactional: cada save corre en su propia transacción local (Spring Data
     * JPA), que se confirma al terminar la llamada. Así la compensación CANCELLED sobrevive
     * aunque el request falle después. SAGA = transacciones locales + compensaciones,
     * nunca una transacción global que se revierta entera.
     */
    public Booking createBooking(Booking booking) {
        // Paso 1: verificar función disponible
        ScreeningDto screening;
        try {
            screening = movieClient.getScreening(booking.getScreeningId());
        } catch (Exception e) {
            throw new InvalidBookingStateException(
                    "No se pudo verificar la función " + booking.getScreeningId() + ": " + e.getMessage());
        }
        if (screening.getAvailableSeats() == null || screening.getAvailableSeats() <= 0) {
            throw new InvalidBookingStateException("Función sin asientos disponibles");
        }

        // Snapshot: se copia del screening al crear la reserva y no cambia después.
        booking.setMovieTitle(screening.getMovieTitle());

        // Paso 2: crear booking en estado PENDING
        booking.setStatus(BookingStatus.PENDING);
        Booking savedBooking = repositoryPort.save(booking);
        UUID bookingId = savedBooking.getId();
        boolean seatReserved = false;

        try {
            // Paso 3: reservar asiento en movie-service (con @Retry)
            movieClient.reserveSeat(savedBooking.getScreeningId());
            seatReserved = true; // ← se actualiza DESPUÉS de completar el paso

            // Paso 4: confirmar booking
            savedBooking.confirm();
            return repositoryPort.save(savedBooking);
        } catch (RuntimeException e) {
            log.error("SAGA de reserva falló. Ejecutando compensaciones. Error: {}", e.getMessage());
            if (seatReserved) {
                compensate("release_seat", () -> movieClient.releaseSeat(savedBooking.getScreeningId()));
            }
            if (bookingId != null) {
                compensate("cancel_booking", () -> {
                    savedBooking.cancel();
                    repositoryPort.save(savedBooking);
                });
            }
            throw translate(e);
        }
    }

    public Booking getById(UUID id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
    }

    /**
     * SAGA inverso de cancelación. El estado CANCELLED se persiste ANTES de la llamada
     * remota: si liberar el asiento falla, se loguea y NO se revierte (best-effort).
     */
    public Booking cancelBooking(UUID id) {
        // Paso 1: verificar que existe (404) y puede cancelarse (422 si ya estaba CANCELLED)
        Booking booking = getById(id);
        booking.cancel();

        // Paso 2: actualizar booking a CANCELLED
        Booking saved = repositoryPort.save(booking);

        // Paso 3: liberar asiento en movie-service (best-effort, absorbido en el adapter)
        movieClient.releaseSeat(saved.getScreeningId());

        return saved;
    }

    private void compensate(String step, Runnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            log.warn("Compensación '{}' falló (best-effort, no se revierte): {}", step, ex.getMessage());
        }
    }

    private RuntimeException translate(RuntimeException e) {
        if (e instanceof InvalidBookingStateException) {
            return e;
        }
        if (e instanceof HttpClientErrorException clientError) {
            // 422 de movie-service (sin asientos) u otro 4xx: violación de invariante.
            return new InvalidBookingStateException("movie-service rechazó la reserva ("
                    + clientError.getStatusCode().value() + "): " + clientError.getMessage());
        }
        // Fallo de red / 5xx / servicio inaccesible tras agotar los reintentos.
        return new SagaFailedException("SAGA abortado tras reintentos: no se pudo contactar con movie-service. "
                + e.getMessage(), e);
    }
}
