package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamJpaRepository extends JpaRepository<TeamEntity, Long> {

    List<TeamEntity> findAllByOrderByIdAsc();
}
