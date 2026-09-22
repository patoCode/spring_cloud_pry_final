package com.cinema.movie_service.application;

import com.cinema.movie_service.domain.Screening;
import com.cinema.movie_service.domain.ScreeningRepositoryPort;
import com.cinema.movie_service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScreeningService {

    private final ScreeningRepositoryPort repositoryPort;
    private final MovieService movieService; // To validate movie exists

    public Screening create(Screening screening) {
        movieService.getById(screening.getMovieId());
        screening.setAvailableSeats(screening.getTotalSeats()); // Initialize available seats
        return repositoryPort.save(screening);
    }

    public Screening getById(UUID id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found with id: " + id));
    }

    public List<Screening> getAll() {
        return repositoryPort.findAll();
    }

    @Transactional
    public Screening reserveSeat(UUID id) {
        Screening screening = getById(id);
        screening.reserve(); // Invoca la invariante de dominio
        return repositoryPort.save(screening);
    }

    @Transactional
    public Screening releaseSeat(UUID id) {
        Screening screening = getById(id);
        screening.release(); // Invoca la invariante de dominio
        return repositoryPort.save(screening);
    }
}
