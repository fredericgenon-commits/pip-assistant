package com.utmost.lu.pipassistant.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.utmost.lu.pipassistant.domain.model.DevComment;
import com.utmost.lu.pipassistant.domain.model.Pip;
import com.utmost.lu.pipassistant.domain.model.PipCapacity;
import com.utmost.lu.pipassistant.domain.model.Project;
import com.utmost.lu.pipassistant.domain.model.Requirement;
import com.utmost.lu.pipassistant.domain.model.Team;
import com.utmost.lu.pipassistant.domain.model.Workload;
import com.utmost.lu.pipassistant.domain.port.PipDetailRepository;
import com.utmost.lu.pipassistant.domain.port.PipRepository;
import com.utmost.lu.pipassistant.domain.port.TeamRepository;

/**
 * Application service for the PIP Details screen: aggregated read, bulk save and the
 * configurable list of requirement statuses.
 */
@Service
public class PipDetailService {

    private final PipRepository pipRepository;
    private final PipDetailRepository detailRepository;
    private final TeamRepository teamRepository;
    private final RequirementStatusCatalog statusCatalog;

    public PipDetailService(
            PipRepository pipRepository,
            PipDetailRepository detailRepository,
            TeamRepository teamRepository,
            RequirementStatusCatalog statusCatalog) {
        this.pipRepository = pipRepository;
        this.detailRepository = detailRepository;
        this.teamRepository = teamRepository;
        this.statusCatalog = statusCatalog;
    }

    public List<String> requirementStatuses() {
        return statusCatalog.all();
    }

    @Transactional(readOnly = true)
    public PipDetailView getDetail(Long pipId) {
        Pip pip = pipRepository.findById(pipId).orElseThrow(() -> new PipNotFoundException(pipId));
        List<Team> teams = teamRepository.findAllOrdered();

        Map<Long, Project> projectsById = detailRepository.findProjectsByPip(pipId).stream()
                .collect(Collectors.toMap(Project::id, Function.identity()));

        Map<Long, Map<Long, BigDecimal>> workloadsByRequirement = detailRepository
                .findWorkloadsByPip(pipId).stream()
                .collect(Collectors.groupingBy(Workload::requirementId,
                        Collectors.toMap(Workload::teamId, Workload::estimate)));

        Map<Long, Map<Long, String>> commentsByRequirement = detailRepository
                .findDevCommentsByPip(pipId).stream()
                .collect(Collectors.groupingBy(DevComment::requirementId,
                        Collectors.toMap(DevComment::teamId, DevComment::text)));

        List<PipDetailView.RequirementRow> rows = detailRepository.findRequirementsByPip(pipId).stream()
                .map(req -> {
                    Project project = projectsById.get(req.projectId());
                    return new PipDetailView.RequirementRow(
                            req.id(),
                            req.projectId(),
                            project != null ? project.tcmKey() : null,
                            project != null ? project.description() : null,
                            req.reqKey(),
                            req.description(),
                            req.status(),
                            req.pmComment(),
                            workloadsByRequirement.getOrDefault(req.id(), Map.of()),
                            commentsByRequirement.getOrDefault(req.id(), Map.of()));
                })
                .toList();

        Map<Long, BigDecimal> capacities = detailRepository.findCapacitiesByPip(pipId).stream()
                .collect(Collectors.toMap(PipCapacity::teamId, PipCapacity::capacity));

        return new PipDetailView(pip, teams, rows, capacities);
    }

    @Transactional
    public void save(Long pipId, SavePipDetailCommand command) {
        Pip pip = pipRepository.findById(pipId).orElseThrow(() -> new PipNotFoundException(pipId));

        // Validate statuses up front so nothing is persisted on a bad request.
        List<String> allowed = statusCatalog.all();
        for (SavePipDetailCommand.RequirementEdit edit : command.requirements()) {
            if (edit.status() != null && !allowed.contains(edit.status())) {
                throw new InvalidRequirementStatusException(edit.status(), allowed);
            }
        }

        // Restrict edits to requirements that actually belong to this PIP.
        Map<Long, Requirement> requirementsById = detailRepository.findRequirementsByPip(pipId).stream()
                .collect(Collectors.toMap(Requirement::id, Function.identity()));

        for (SavePipDetailCommand.RequirementEdit edit : command.requirements()) {
            Requirement existing = requirementsById.get(edit.id());
            if (existing == null) {
                continue; // ignore unknown / foreign requirement ids
            }
            detailRepository.updateRequirement(edit.id(), edit.description(), edit.status(), edit.pmComment());
            detailRepository.updateProjectDescription(existing.projectId(), edit.tcmDescription());
            normalize(edit.workloads()).forEach(
                    (teamId, estimate) -> detailRepository.upsertWorkload(edit.id(), teamId, estimate));
            normalize(edit.comments()).forEach(
                    (teamId, text) -> detailRepository.upsertDevComment(edit.id(), teamId, text));
        }

        normalize(command.capacities()).forEach(
                (teamId, capacity) -> detailRepository.upsertCapacity(pip.id(), teamId, capacity));
    }

    private static <V> Map<Long, V> normalize(Map<Long, V> map) {
        return map != null ? map : Map.of();
    }

    /** Interim creation entry point (tests / future Excel/JIRA import). */
    @Transactional
    public Requirement createRequirement(
            Long pipId,
            String tcmKey,
            String tcmDescription,
            String reqKey,
            String description,
            String status,
            String pmComment) {
        pipRepository.findById(pipId).orElseThrow(() -> new PipNotFoundException(pipId));
        List<String> allowed = statusCatalog.all();
        if (status != null && !allowed.contains(status)) {
            throw new InvalidRequirementStatusException(status, allowed);
        }
        return detailRepository.createRequirement(
                pipId, tcmKey, tcmDescription, reqKey, description, status, pmComment);
    }
}
