package com.cinema.movie_service.infrastructure.persistence.repository;

import com.cinema.movie_service.infrastructure.persistence.entity.ScreeningEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ScreeningJpaRepository extends JpaRepository<ScreeningEntity, UUID> {
}
