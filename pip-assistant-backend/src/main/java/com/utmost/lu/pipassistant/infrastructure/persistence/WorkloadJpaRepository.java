package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkloadJpaRepository extends JpaRepository<WorkloadEntity, Long> {

    List<WorkloadEntity> findByRequirementIdIn(Collection<Long> requirementIds);

    Optional<WorkloadEntity> findByRequirementIdAndTeamId(Long requirementId, Long teamId);
}
