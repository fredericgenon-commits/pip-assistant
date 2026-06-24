package com.utmost.lu.pipassistant.application;

import java.math.BigDecimal;
import java.util.Comparator;
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

/** Application service for the PIP Details screen: aggregated read and bulk save. */
@Service
public class PipDetailService {

    private final PipRepository pipRepository;
    private final PipDetailRepository detailRepository;
    private final TeamRepository teamRepository;

    public PipDetailService(
            PipRepository pipRepository,
            PipDetailRepository detailRepository,
            TeamRepository teamRepository) {
        this.pipRepository = pipRepository;
        this.detailRepository = detailRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public PipDetailView getDetail(Long pipId) {
        Pip pip = pipRepository.findById(pipId).orElseThrow(() -> new PipNotFoundException(pipId));
        List<Team> teams = teamRepository.findAllOrdered();

        Map<Long, Project> projectsById = detailRepository.findProjectsByPip(pipId).stream()
                .collect(Collectors.toMap(Project::id, Function.identity()));

        Map<Long, Map<Long, String>> workloadsByRequirement = detailRepository
                .findWorkloadsByPip(pipId).stream()
                .filter(w -> w.tbd() || w.estimate() != null)
                .collect(Collectors.groupingBy(Workload::requirementId,
                        Collectors.toMap(Workload::teamId, PipDetailService::cellText)));

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
                            req.priority(),
                            req.pipStatus(),
                            workloadsByRequirement.getOrDefault(req.id(), Map.of()),
                            commentsByRequirement.getOrDefault(req.id(), Map.of()));
                })
                .sorted(Comparator
                        .comparing((PipDetailView.RequirementRow r) -> r.priority() == null)
                        .thenComparing(r -> r.priority() == null ? Integer.MAX_VALUE : r.priority())
                        .thenComparing(PipDetailView.RequirementRow::reqKey))
                .toList();

        Map<Long, BigDecimal> capacities = detailRepository.findCapacitiesByPip(pipId).stream()
                .collect(Collectors.toMap(PipCapacity::teamId, PipCapacity::capacity));

        return new PipDetailView(pip, teams, rows, capacities);
    }

    @Transactional
    public void save(Long pipId, SavePipDetailCommand command) {
        Pip pip = pipRepository.findById(pipId).orElseThrow(() -> new PipNotFoundException(pipId));

        // Restrict edits to requirements that actually belong to this PIP.
        Map<Long, Requirement> requirementsById = detailRepository.findRequirementsByPip(pipId).stream()
                .collect(Collectors.toMap(Requirement::id, Function.identity()));

        for (SavePipDetailCommand.RequirementEdit edit : command.requirements()) {
            Requirement existing = requirementsById.get(edit.id());
            if (existing == null) {
                continue; // ignore unknown / foreign requirement ids
            }
            detailRepository.updateRequirement(edit.id(), edit.description(), edit.pmComment());
            detailRepository.updateProjectDescription(existing.projectId(), edit.tcmDescription());
            normalize(edit.workloads()).forEach((teamId, cell) -> {
                WorkloadCell parsed = parseWorkloadCell(cell);
                if (parsed != null) {
                    detailRepository.upsertWorkload(edit.id(), teamId, parsed.estimate(), parsed.tbd());
                }
            });
            normalize(edit.comments()).forEach(
                    (teamId, text) -> detailRepository.upsertDevComment(edit.id(), teamId, text));
        }

        normalize(command.capacities()).forEach(
                (teamId, capacity) -> detailRepository.upsertCapacity(pip.id(), teamId, capacity));
    }

    private static <V> Map<Long, V> normalize(Map<Long, V> map) {
        return map != null ? map : Map.of();
    }

    private static final String TBD = "TBD";

    /** Renders a stored workload as the cell text shown in the grid (a number or "TBD"). */
    private static String cellText(Workload workload) {
        if (workload.tbd()) {
            return TBD;
        }
        BigDecimal estimate = workload.estimate();
        if (estimate.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }
        return estimate.stripTrailingZeros().toPlainString();
    }

    /**
     * Parses a workload cell into an (estimate, tbd) pair. Empty/blank clears the cell;
     * "TBD" (case-insensitive) marks it To Be Defined; otherwise it must be a number. Returns
     * null for an unparseable value so the existing cell is left untouched.
     */
    private static WorkloadCell parseWorkloadCell(String cell) {
        String trimmed = cell == null ? "" : cell.trim();
        if (trimmed.isEmpty()) {
            return new WorkloadCell(null, false);
        }
        if (TBD.equalsIgnoreCase(trimmed)) {
            return new WorkloadCell(null, true);
        }
        try {
            return new WorkloadCell(new BigDecimal(trimmed), false);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private record WorkloadCell(BigDecimal estimate, boolean tbd) {
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
        return detailRepository.createRequirement(
                pipId, tcmKey, tcmDescription, reqKey, description, status, pmComment);
    }
}
