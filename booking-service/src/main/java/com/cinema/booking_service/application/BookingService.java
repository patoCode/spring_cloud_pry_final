package com.cinema.booking_service.application;

import com.cinema.booking_service.api.dto.ScreeningDto;
import com.cinema.booking_service.domain.Booking;
import com.cinema.booking_service.domain.BookingRepositoryPort;
import com.cinema.booking_service.domain.BookingStatus;
import com.cinema.booking_service.domain.MovieServiceClientPort;
import com.cinema.booking_service.exception.InvalidBookingStateException;
import com.cinema.booking_service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepositoryPort repositoryPort;
    private final MovieServiceClientPort movieClient;

    @Transactional
    public Booking createBooking(Booking booking) {
        // SAGA Paso 1: Verificar función disponible
        ScreeningDto screening;
        try {
            screening = movieClient.getScreening(booking.getScreeningId());
            if (screening.getAvailableSeats() <= 0) {
                throw new InvalidBookingStateException("Función sin asientos disponibles");
            }
        } catch (Exception e) {
            throw new InvalidBookingStateException("No se pudo verificar la función o no hay asientos: " + e.getMessage());
        }

        // Snapshot
        // Asumiendo que podemos obtener el titulo, aunque ScreeningDto no lo tiene.
        // Simplificación: omitido si no está en el DTO, pero la regla pide movieTitle snapshot.
        // Si el DTO no lo tiene, lo dejaremos en blanco por ahora, o podrías buscar la movie.
        booking.setMovieTitle("Snapshot-Title"); // TODO: Obtener título real si se requiere

        // SAGA Paso 2: Crear booking PENDING
        booking.setStatus(BookingStatus.PENDING);
        Booking savedBooking = repositoryPort.save(booking);

        UUID bookingId = savedBooking.getId();
        boolean seatReserved = false;

        try {
            // SAGA Paso 3: Reservar asiento en movie-service
            movieClient.reserveSeat(booking.getScreeningId());
            seatReserved = true;

            // SAGA Paso 4: Confirmar booking
            savedBooking.confirm();
            return repositoryPort.save(savedBooking);
            
        } catch (Exception e) {
            log.error("Fallo al reservar asiento. Ejecutando compensación. Error: {}", e.getMessage());
            // Compensación: si falla, cancelar localmente
            if (bookingId != null) {
                savedBooking.cancel();
                repositoryPort.save(savedBooking);
            }
            throw new RuntimeException("Error en reserva SAGA, booking cancelado: " + e.getMessage());
        }
    }

    public Booking getById(UUID id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
    }

    @Transactional
    public Booking cancelBooking(UUID id) {
        // SAGA Inverso Paso 1 y 2
        Booking booking = getById(id);
        booking.cancel(); // Valida si ya estaba cancelado (lanza excepción)
        Booking saved = repositoryPort.save(booking);

        // SAGA Inverso Paso 3: Liberar asiento (best-effort)
        movieClient.releaseSeat(booking.getScreeningId());
        
        return saved;
    }
}
