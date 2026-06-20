package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, Long> {

    List<ProjectEntity> findByPipId(Long pipId);

    Optional<ProjectEntity> findByPipIdAndTcmKey(Long pipId, String tcmKey);
}
