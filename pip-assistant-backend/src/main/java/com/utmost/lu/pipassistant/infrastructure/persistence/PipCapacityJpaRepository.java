package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PipCapacityJpaRepository extends JpaRepository<PipCapacityEntity, Long> {

    List<PipCapacityEntity> findByPipId(Long pipId);

    Optional<PipCapacityEntity> findByPipIdAndTeamId(Long pipId, Long teamId);
}
