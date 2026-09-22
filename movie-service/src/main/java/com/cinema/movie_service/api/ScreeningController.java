package com.cinema.movie_service.api;

import com.cinema.movie_service.api.dto.ScreeningRequest;
import com.cinema.movie_service.api.dto.ScreeningResponse;
import com.cinema.movie_service.application.ScreeningService;
import com.cinema.movie_service.domain.Screening;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/screenings")
@RequiredArgsConstructor
@Tag(name = "Screenings", description = "Endpoints for Screenings")
public class ScreeningController {

    private final ScreeningService service;

    @PostMapping
    @Operation(summary = "Create a new screening")
    public ResponseEntity<ScreeningResponse> createScreening(@Valid @RequestBody ScreeningRequest request) {
        Screening screening = Screening.builder()
                .movieId(request.getMovieId())
                .room(request.getRoom())
                .showAt(request.getShowAt())
                .totalSeats(request.getTotalSeats())
                .active(true)
                .build();
        Screening saved = service.create(screening);
        
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
                
        return ResponseEntity.created(location).body(toResponse(saved));
    }

    @GetMapping
    @Operation(summary = "Get all screenings")
    public ResponseEntity<List<ScreeningResponse>> getAllScreenings() {
        return ResponseEntity.ok(service.getAll().stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a screening by ID")
    public ResponseEntity<ScreeningResponse> getScreeningById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.getById(id)));
    }

    @PatchMapping("/{id}/reserve")
    @Operation(summary = "Reserve a seat for a screening")
    public ResponseEntity<ScreeningResponse> reserveSeat(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.reserveSeat(id)));
    }

    @PatchMapping("/{id}/release")
    @Operation(summary = "Release a seat for a screening")
    public ResponseEntity<ScreeningResponse> releaseSeat(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.releaseSeat(id)));
    }

    private ScreeningResponse toResponse(Screening screening) {
        return ScreeningResponse.builder()
                .id(screening.getId())
                .movieId(screening.getMovieId())
                .room(screening.getRoom())
                .showAt(screening.getShowAt())
                .totalSeats(screening.getTotalSeats())
                .availableSeats(screening.getAvailableSeats())
                .active(screening.isActive())
                .build();
    }
}
