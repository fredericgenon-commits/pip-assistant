package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.utmost.lu.pipassistant.domain.model.DevComment;
import com.utmost.lu.pipassistant.domain.model.PipCapacity;
import com.utmost.lu.pipassistant.domain.model.Project;
import com.utmost.lu.pipassistant.domain.model.Requirement;
import com.utmost.lu.pipassistant.domain.model.Workload;
import com.utmost.lu.pipassistant.domain.port.PipDetailRepository;

/**
 * Adapter for the PIP detail aggregate, composing the per-table Spring Data repositories.
 */
@Component
public class PipDetailRepositoryAdapter implements PipDetailRepository {

    private final ProjectJpaRepository projectRepository;
    private final RequirementJpaRepository requirementRepository;
    private final WorkloadJpaRepository workloadRepository;
    private final DevCommentJpaRepository devCommentRepository;
    private final PipCapacityJpaRepository capacityRepository;

    public PipDetailRepositoryAdapter(
            ProjectJpaRepository projectRepository,
            RequirementJpaRepository requirementRepository,
            WorkloadJpaRepository workloadRepository,
            DevCommentJpaRepository devCommentRepository,
            PipCapacityJpaRepository capacityRepository) {
        this.projectRepository = projectRepository;
        this.requirementRepository = requirementRepository;
        this.workloadRepository = workloadRepository;
        this.devCommentRepository = devCommentRepository;
        this.capacityRepository = capacityRepository;
    }

    @Override
    public List<Project> findProjectsByPip(Long pipId) {
        return projectRepository.findByPipId(pipId).stream()
                .map(e -> new Project(e.getId(), e.getTcmKey(), e.getDescription(), e.getPipId()))
                .toList();
    }

    @Override
    public List<Requirement> findRequirementsByPip(Long pipId) {
        List<Long> projectIds = projectRepository.findByPipId(pipId).stream()
                .map(ProjectEntity::getId).toList();
        if (projectIds.isEmpty()) {
            return List.of();
        }
        return requirementRepository.findByProjectIdIn(projectIds).stream()
                .map(e -> new Requirement(e.getId(), e.getReqKey(), e.getDescription(),
                        e.getStatus(), e.getPmComment(), e.getProjectId()))
                .toList();
    }

    @Override
    public List<Workload> findWorkloadsByPip(Long pipId) {
        List<Long> requirementIds = requirementIdsOf(pipId);
        if (requirementIds.isEmpty()) {
            return List.of();
        }
        return workloadRepository.findByRequirementIdIn(requirementIds).stream()
                .map(e -> new Workload(e.getRequirementId(), e.getTeamId(), e.getEstimate()))
                .toList();
    }

    @Override
    public List<DevComment> findDevCommentsByPip(Long pipId) {
        List<Long> requirementIds = requirementIdsOf(pipId);
        if (requirementIds.isEmpty()) {
            return List.of();
        }
        return devCommentRepository.findByRequirementIdIn(requirementIds).stream()
                .map(e -> new DevComment(e.getRequirementId(), e.getTeamId(), e.getText()))
                .toList();
    }

    @Override
    public List<PipCapacity> findCapacitiesByPip(Long pipId) {
        return capacityRepository.findByPipId(pipId).stream()
                .map(e -> new PipCapacity(e.getPipId(), e.getTeamId(), e.getCapacity()))
                .toList();
    }

    @Override
    public void updateProjectDescription(Long projectId, String description) {
        projectRepository.findById(projectId).ifPresent(e -> {
            e.setDescription(description);
            projectRepository.save(e);
        });
    }

    @Override
    public void updateRequirement(Long requirementId, String description, String status, String pmComment) {
        requirementRepository.findById(requirementId).ifPresent(e -> {
            e.setDescription(description);
            e.setStatus(status);
            e.setPmComment(pmComment);
            requirementRepository.save(e);
        });
    }

    @Override
    public void upsertWorkload(Long requirementId, Long teamId, BigDecimal estimate) {
        WorkloadEntity entity = workloadRepository.findByRequirementIdAndTeamId(requirementId, teamId)
                .orElseGet(() -> new WorkloadEntity(requirementId, teamId, null));
        entity.setEstimate(estimate);
        workloadRepository.save(entity);
    }

    @Override
    public void upsertDevComment(Long requirementId, Long teamId, String text) {
        DevCommentEntity entity = devCommentRepository.findByRequirementIdAndTeamId(requirementId, teamId)
                .orElseGet(() -> new DevCommentEntity(requirementId, teamId, null));
        entity.setText(text);
        devCommentRepository.save(entity);
    }

    @Override
    public void upsertCapacity(Long pipId, Long teamId, BigDecimal capacity) {
        PipCapacityEntity entity = capacityRepository.findByPipIdAndTeamId(pipId, teamId)
                .orElseGet(() -> new PipCapacityEntity(pipId, teamId, null));
        entity.setCapacity(capacity);
        capacityRepository.save(entity);
    }

    @Override
    public Requirement createRequirement(Long pipId, String tcmKey, String tcmDescription,
                                         String reqKey, String description, String status, String pmComment) {
        ProjectEntity project = projectRepository.findByPipIdAndTcmKey(pipId, tcmKey)
                .orElseGet(() -> projectRepository.save(new ProjectEntity(null, tcmKey, tcmDescription, pipId)));
        RequirementEntity saved = requirementRepository.save(
                new RequirementEntity(null, reqKey, description, status, pmComment, project.getId()));
        return new Requirement(saved.getId(), saved.getReqKey(), saved.getDescription(),
                saved.getStatus(), saved.getPmComment(), saved.getProjectId());
    }

    private List<Long> requirementIdsOf(Long pipId) {
        List<Long> projectIds = projectRepository.findByPipId(pipId).stream()
                .map(ProjectEntity::getId).toList();
        if (projectIds.isEmpty()) {
            return List.of();
        }
        return requirementRepository.findByProjectIdIn(projectIds).stream()
                .map(RequirementEntity::getId).toList();
    }
}
