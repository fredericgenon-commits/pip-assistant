package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DevCommentJpaRepository extends JpaRepository<DevCommentEntity, Long> {

    List<DevCommentEntity> findByRequirementIdIn(Collection<Long> requirementIds);

    Optional<DevCommentEntity> findByRequirementIdAndTeamId(Long requirementId, Long teamId);
}
