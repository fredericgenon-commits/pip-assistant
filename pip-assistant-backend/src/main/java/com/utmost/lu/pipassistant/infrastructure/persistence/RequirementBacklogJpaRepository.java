package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RequirementBacklogJpaRepository extends JpaRepository<RequirementBacklogEntity, Long> {

    Optional<RequirementBacklogEntity> findByRequirementIdAndTeamId(Long requirementId, Long teamId);

    /** All backlog entries for requirements belonging to the given PIP. */
    @Query("""
            SELECT rb FROM RequirementBacklogEntity rb
            WHERE rb.requirementId IN (
                SELECT r.id FROM RequirementEntity r
                WHERE r.projectId IN (
                    SELECT p.id FROM ProjectEntity p WHERE p.pipId = :pipId
                )
            )
            """)
    List<RequirementBacklogEntity> findByPipId(Long pipId);
}
