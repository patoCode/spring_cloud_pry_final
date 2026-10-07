package com.cinema.movie_service.api;

import com.cinema.movie_service.api.dto.ScreeningRequest;
import com.cinema.movie_service.api.dto.ScreeningResponse;
import com.cinema.movie_service.application.ScreeningService;
import com.cinema.movie_service.domain.Screening;
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
    @Operation(summary = "Create a new screening", description = "Crea una función. Requiere JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Función creada con header Location",
                    content = @Content(schema = @Schema(implementation = ScreeningResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validación de campos fallida",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Película inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
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
    @Operation(summary = "Get all screenings", description = "Listado de funciones con asientos disponibles.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de funciones",
                    content = @Content(schema = @Schema(implementation = ScreeningResponse.class)))
    })
    public ResponseEntity<List<ScreeningResponse>> getAllScreenings() {
        return ResponseEntity.ok(service.getAll().stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a screening by ID", description = "Devuelve una función por su UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Función encontrada",
                    content = @Content(schema = @Schema(implementation = ScreeningResponse.class))),
            @ApiResponse(responseCode = "404", description = "Función inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ScreeningResponse> getScreeningById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.getById(id)));
    }

    @PatchMapping("/{id}/reserve")
    @Operation(summary = "Reserve a seat for a screening",
            description = "Decrementa availableSeats aplicando la invariante de dominio. Requiere JWT. Paso 3 del SAGA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asiento reservado",
                    content = @Content(schema = @Schema(implementation = ScreeningResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Función inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Función modificada por otra operación concurrente (bloqueo optimista)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Función sin asientos disponibles",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ScreeningResponse> reserveSeat(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.reserveSeat(id)));
    }

    @PatchMapping("/{id}/release")
    @Operation(summary = "Release a seat for a screening",
            description = "Incrementa availableSeats (compensación best-effort del SAGA). Requiere JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asiento liberado",
                    content = @Content(schema = @Schema(implementation = ScreeningResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Función inexistente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Función modificada por otra operación concurrente (bloqueo optimista)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ScreeningResponse> releaseSeat(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.releaseSeat(id)));
    }

    private ScreeningResponse toResponse(Screening screening) {
        return ScreeningResponse.builder()
                .id(screening.getId())
                .movieId(screening.getMovieId())
                .movieTitle(screening.getMovieTitle())
                .room(screening.getRoom())
                .showAt(screening.getShowAt())
                .totalSeats(screening.getTotalSeats())
                .availableSeats(screening.getAvailableSeats())
                .active(screening.isActive())
                .build();
    }
}
