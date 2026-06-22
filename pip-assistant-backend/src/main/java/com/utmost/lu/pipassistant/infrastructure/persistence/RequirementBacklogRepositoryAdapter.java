package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.utmost.lu.pipassistant.domain.port.RequirementBacklogRepository;

@Component
public class RequirementBacklogRepositoryAdapter implements RequirementBacklogRepository {

    private final RequirementBacklogJpaRepository jpaRepository;

    public RequirementBacklogRepositoryAdapter(RequirementBacklogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void upsertTeamStatus(Long requirementId, Long teamId, String teamStatus) {
        RequirementBacklogEntity entity = jpaRepository
                .findByRequirementIdAndTeamId(requirementId, teamId)
                .orElseGet(() -> new RequirementBacklogEntity(requirementId, teamId, null));
        entity.setTeamStatus(teamStatus);
        jpaRepository.save(entity);
    }

    @Override
    public List<TeamBacklogEntry> findByPip(Long pipId) {
        return jpaRepository.findByPipId(pipId).stream()
                .map(e -> new TeamBacklogEntry(e.getRequirementId(), e.getTeamId(), e.getTeamStatus()))
                .toList();
    }
}
