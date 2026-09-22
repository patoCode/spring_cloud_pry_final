package com.cinema.movie_service.api;

import com.cinema.movie_service.api.dto.MovieRequest;
import com.cinema.movie_service.api.dto.MovieResponse;
import com.cinema.movie_service.application.MovieService;
import com.cinema.movie_service.domain.Movie;
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
@RequestMapping("/movies")
@RequiredArgsConstructor
@Tag(name = "Movies", description = "Endpoints for Movies")
public class MovieController {

    private final MovieService service;

    @PostMapping
    @Operation(summary = "Create a new movie")
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .director(request.getDirector())
                .genre(request.getGenre())
                .durationMin(request.getDurationMin())
                .rating(request.getRating())
                .active(true)
                .build();
        Movie saved = service.create(movie);
        
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
                
        return ResponseEntity.created(location).body(toResponse(saved));
    }

    @GetMapping
    @Operation(summary = "Get all movies")
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        return ResponseEntity.ok(service.getAll().stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a movie by ID")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing movie")
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable UUID id, @Valid @RequestBody MovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .director(request.getDirector())
                .genre(request.getGenre())
                .durationMin(request.getDurationMin())
                .rating(request.getRating())
                .build();
        return ResponseEntity.ok(toResponse(service.update(id, movie)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a movie by ID")
    public ResponseEntity<Void> deleteMovie(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a movie")
    public ResponseEntity<MovieResponse> activateMovie(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.setStatus(id, true)));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a movie")
    public ResponseEntity<MovieResponse> deactivateMovie(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.setStatus(id, false)));
    }

    private MovieResponse toResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .director(movie.getDirector())
                .genre(movie.getGenre())
                .durationMin(movie.getDurationMin())
                .rating(movie.getRating())
                .active(movie.isActive())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .build();
    }
}
