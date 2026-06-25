package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.utmost.lu.pipassistant.domain.model.DiffedRequirement;
import com.utmost.lu.pipassistant.domain.model.ImportDiff;
import com.utmost.lu.pipassistant.domain.model.ParsedRequirement;
import com.utmost.lu.pipassistant.domain.model.RequirementPipStatus;
import com.utmost.lu.pipassistant.domain.model.SnapshotRequirement;
import com.utmost.lu.pipassistant.domain.port.ExcelImportRepository;

/**
 * Adapter for Excel import versioning. Stores the raw snapshot of each version and updates
 * the live requirement/workload tables, honouring manual workload overrides.
 */
@Component
public class ExcelImportRepositoryAdapter implements ExcelImportRepository {

    private final ExcelImportJpaRepository imports;
    private final ImportedRequirementJpaRepository importedRequirements;
    private final ImportedWorkloadJpaRepository importedWorkloads;
    private final ProjectJpaRepository projects;
    private final RequirementJpaRepository requirements;
    private final WorkloadJpaRepository workloads;
    private final TeamJpaRepository teams;

    public ExcelImportRepositoryAdapter(
            ExcelImportJpaRepository imports,
            ImportedRequirementJpaRepository importedRequirements,
            ImportedWorkloadJpaRepository importedWorkloads,
            ProjectJpaRepository projects,
            RequirementJpaRepository requirements,
            WorkloadJpaRepository workloads,
            TeamJpaRepository teams) {
        this.imports = imports;
        this.importedRequirements = importedRequirements;
        this.importedWorkloads = importedWorkloads;
        this.projects = projects;
        this.requirements = requirements;
        this.workloads = workloads;
        this.teams = teams;
    }

    @Override
    public int nextVersionNo(Long pipId) {
        return imports.findFirstByPipIdOrderByVersionNoDesc(pipId)
                .map(e -> e.getVersionNo() + 1)
                .orElse(1);
    }

    @Override
    public Optional<ExcelImportRepository.ImportMeta> findLastImportMeta(Long pipId) {
        return imports.findFirstByPipIdOrderByVersionNoDesc(pipId)
                .map(e -> new ExcelImportRepository.ImportMeta(
                        e.getVersionNo(), e.getOriginalFilename(), e.getImportedAt()));
    }

    @Override
    public List<SnapshotRequirement> findLatestSnapshot(Long pipId) {
        return imports.findFirstByPipIdOrderByVersionNoDesc(pipId)
                .map(this::toSnapshot)
                .orElseGet(List::of);
    }

    private List<SnapshotRequirement> toSnapshot(ExcelImportEntity version) {
        List<ImportedRequirementEntity> rows = importedRequirements.findByExcelImportId(version.getId());
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<Long, String> teamNamesById = teamNamesById();
        Map<Long, List<ImportedWorkloadEntity>> workloadsByReq = importedWorkloads
                .findByImportedRequirementIdIn(rows.stream().map(ImportedRequirementEntity::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(ImportedWorkloadEntity::getImportedRequirementId));

        return rows.stream().map(row -> {
            Map<String, java.math.BigDecimal> wl = workloadsByReq.getOrDefault(row.getId(), List.of()).stream()
                    .filter(w -> teamNamesById.containsKey(w.getTeamId()) && w.getEstimate() != null)
                    .collect(Collectors.toMap(w -> teamNamesById.get(w.getTeamId()), ImportedWorkloadEntity::getEstimate));
            return new SnapshotRequirement(row.getReqKey(), row.getTcmKey(), row.getTcmDescription(),
                    row.getReqDescription(), row.getPmComment(), row.getPriority(), wl);
        }).toList();
    }

    @Override
    public void applyImport(Long pipId, int versionNo, String originalFilename, Instant importedAt, ImportDiff diff) {
        Map<String, Long> teamIdByName = teams.findAll().stream()
                .collect(Collectors.toMap(TeamEntity::getName, TeamEntity::getId));
        ExcelImportEntity version = imports.save(
                new ExcelImportEntity(pipId, versionNo, originalFilename, importedAt));
        Map<String, RequirementEntity> liveByKey = liveRequirementsByKey(pipId);

        for (DiffedRequirement diffed : diff.current()) {
            ParsedRequirement parsed = diffed.parsed();
            storeSnapshotRow(version.getId(), diffed, teamIdByName);

            RequirementEntity requirement = upsertLiveRequirement(pipId, diffed, liveByKey);
            upsertLiveWorkloads(requirement.getId(), parsed, teamIdByName);
        }

        for (String removedKey : diff.removedReqKeys()) {
            RequirementEntity requirement = liveByKey.get(removedKey);
            if (requirement != null) {
                requirement.setPipStatus(RequirementPipStatus.REMOVED_FROM_PIP.name());
                requirement.setPriority(null);
                requirements.save(requirement);
            }
        }
    }

    private void storeSnapshotRow(Long versionId, DiffedRequirement diffed, Map<String, Long> teamIdByName) {
        ParsedRequirement parsed = diffed.parsed();
        ImportedRequirementEntity row = importedRequirements.save(new ImportedRequirementEntity(
                versionId, parsed.reqKey(), parsed.tcmKey(), parsed.tcmDescription(),
                parsed.reqDescription(), parsed.pmComment(), diffed.priority(), parsed.missingData()));
        parsed.workloadsByTeam().forEach((teamName, estimate) -> {
            Long teamId = teamIdByName.get(teamName);
            if (teamId != null) {
                importedWorkloads.save(new ImportedWorkloadEntity(row.getId(), teamId, estimate));
            }
        });
    }

    private RequirementEntity upsertLiveRequirement(Long pipId, DiffedRequirement diffed,
                                                    Map<String, RequirementEntity> liveByKey) {
        ParsedRequirement parsed = diffed.parsed();
        String tcmKey = parsed.tcmKey() != null ? parsed.tcmKey() : "";
        ProjectEntity project = projects.findByPipIdAndTcmKey(pipId, tcmKey)
                .orElseGet(() -> projects.save(new ProjectEntity(null, tcmKey, parsed.tcmDescription(), pipId)));
        if (!Objects.equals(project.getDescription(), parsed.tcmDescription())) {
            project.setDescription(parsed.tcmDescription());
            projects.save(project);
        }

        RequirementEntity requirement = liveByKey.get(parsed.reqKey());
        if (requirement == null) {
            requirement = new RequirementEntity(null, parsed.reqKey(), parsed.reqDescription(),
                    null, parsed.pmComment(), project.getId());
        } else {
            requirement.setDescription(parsed.reqDescription());
            requirement.setPmComment(parsed.pmComment());
            requirement.setProjectId(project.getId());
        }
        requirement.setPriority(diffed.priority());
        requirement.setPipStatus(diffed.status().name());
        RequirementEntity saved = requirements.save(requirement);
        liveByKey.put(parsed.reqKey(), saved);
        return saved;
    }

    /** Apply file workloads to non-overridden cells (manual edits are preserved). */
    private void upsertLiveWorkloads(Long requirementId, ParsedRequirement parsed, Map<String, Long> teamIdByName) {
        parsed.workloadsByTeam().forEach((teamName, estimate) -> {
            Long teamId = teamIdByName.get(teamName);
            if (teamId == null) {
                return;
            }
            WorkloadEntity workload = workloads.findByRequirementIdAndTeamId(requirementId, teamId)
                    .orElseGet(() -> new WorkloadEntity(requirementId, teamId, null));
            if (workload.isManualOverride()) {
                return; // keep the user's value
            }
            workload.setEstimate(estimate);
            workloads.save(workload);
        });
    }

    private Map<String, RequirementEntity> liveRequirementsByKey(Long pipId) {
        List<Long> projectIds = projects.findByPipId(pipId).stream().map(ProjectEntity::getId).toList();
        if (projectIds.isEmpty()) {
            return new HashMap<>();
        }
        return requirements.findByProjectIdIn(projectIds).stream()
                .collect(Collectors.toMap(RequirementEntity::getReqKey, Function.identity(),
                        (a, b) -> a, HashMap::new));
    }

    private Map<Long, String> teamNamesById() {
        return teams.findAll().stream().collect(Collectors.toMap(TeamEntity::getId, TeamEntity::getName));
    }
}
