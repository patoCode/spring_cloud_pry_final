package com.cinema.booking_service.api;

import com.cinema.booking_service.api.dto.BookingRequest;
import com.cinema.booking_service.api.dto.BookingResponse;
import com.cinema.booking_service.application.BookingService;
import com.cinema.booking_service.domain.Booking;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Endpoints for Bookings SAGA")
public class BookingController {

    private final BookingService service;

    @PostMapping
    @Operation(summary = "Create a booking and execute SAGA",
            description = "Orquesta el SAGA de 4 pasos: verifica la función, crea la reserva PENDING, "
                    + "reserva el asiento en movie-service y confirma la reserva. Requiere JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva creada y CONFIRMED con header Location",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validación de campos fallida",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Función sin asientos disponibles o función no verificable (compensación ejecutada)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "502", description = "Fallo de infraestructura contra movie-service tras los reintentos (compensación ejecutada)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        Booking booking = Booking.builder()
                .screeningId(request.getScreeningId())
                .customerId(request.getCustomerId())
                .totalAmount(request.getTotalAmount())
                .build();

        Booking saved = service.createBooking(booking);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a booking by ID",
            description = "Devuelve la reserva usando únicamente su snapshot local (sin llamar a movie-service).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva encontrada",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Reserva inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.getById(id)));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking and execute inverse SAGA",
            description = "Marca la reserva CANCELLED y libera el asiento en movie-service (best-effort). Requiere JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva cancelada (CANCELLED)",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Reserva inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "La reserva ya estaba CANCELLED",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.cancelBooking(id)));
    }

    private BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .screeningId(booking.getScreeningId())
                .movieTitle(booking.getMovieTitle())
                .customerId(booking.getCustomerId())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
