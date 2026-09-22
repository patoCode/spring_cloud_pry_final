package com.cinema.booking_service.api;

import com.cinema.booking_service.api.dto.BookingRequest;
import com.cinema.booking_service.api.dto.BookingResponse;
import com.cinema.booking_service.application.BookingService;
import com.cinema.booking_service.domain.Booking;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    @Operation(summary = "Create a booking and execute SAGA")
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
    @Operation(summary = "Get a booking by ID")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.getById(id)));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking and execute inverse SAGA")
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
