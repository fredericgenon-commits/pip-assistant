package com.utmost.lu.pipassistant.infrastructure.web.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.utmost.lu.pipassistant.application.PipDetailView;

/** Aggregated payload for the PIP Details screen. */
public record PipDetailResponse(
        PipResponse pip,
        List<TeamResponse> teams,
        List<RequirementRowResponse> requirements,
        Map<Long, BigDecimal> capacities,
        LastImportResponse lastImport) {

    public record LastImportResponse(int versionNo, String originalFilename, String importedAt) {}

    public static PipDetailResponse from(PipDetailView view, String jiraBaseUrl) {
        LastImportResponse li = view.lastImport() != null
                ? new LastImportResponse(
                        view.lastImport().versionNo(),
                        view.lastImport().originalFilename(),
                        view.lastImport().importedAt().toString())
                : null;
        return new PipDetailResponse(
                PipResponse.from(view.pip()),
                view.teams().stream().map(TeamResponse::from).toList(),
                view.requirements().stream()
                        .map(r -> RequirementRowResponse.from(r, jiraBaseUrl))
                        .toList(),
                view.capacities(),
                li);
    }
}
