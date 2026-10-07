package com.cinema.movie_service.application;

import com.cinema.movie_service.domain.Movie;
import com.cinema.movie_service.domain.Screening;
import com.cinema.movie_service.domain.ScreeningRepositoryPort;
import com.cinema.movie_service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScreeningService {

    private final ScreeningRepositoryPort repositoryPort;
    private final MovieService movieService; // To validate movie exists

    public Screening create(Screening screening) {
        movieService.getById(screening.getMovieId());
        screening.setAvailableSeats(screening.getTotalSeats()); // Initialize available seats
        return withMovieTitle(repositoryPort.save(screening));
    }

    public Screening getById(UUID id) {
        Screening screening = repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found with id: " + id));
        return withMovieTitle(screening);
    }

    public List<Screening> getAll() {
        Map<UUID, String> titles = movieService.getAll().stream()
                .filter(movie -> movie.getId() != null)
                .collect(Collectors.toMap(Movie::getId, Movie::getTitle, (first, second) -> first));
        return repositoryPort.findAll().stream()
                .peek(screening -> screening.setMovieTitle(titles.get(screening.getMovieId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public Screening reserveSeat(UUID id) {
        Screening screening = repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found with id: " + id));
        screening.reserve(); // Invoca la invariante de dominio
        return withMovieTitle(repositoryPort.save(screening));
    }

    @Transactional
    public Screening releaseSeat(UUID id) {
        Screening screening = repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found with id: " + id));
        screening.release(); // Invoca la invariante de dominio
        return withMovieTitle(repositoryPort.save(screening));
    }

    private Screening withMovieTitle(Screening screening) {
        if (screening == null || screening.getMovieId() == null) {
            return screening;
        }
        try {
            screening.setMovieTitle(movieService.getById(screening.getMovieId()).getTitle());
        } catch (ResourceNotFoundException ignored) {
            // Película eliminada: la función expone el título desconocido en lugar de fallar.
        }
        return screening;
    }
}
