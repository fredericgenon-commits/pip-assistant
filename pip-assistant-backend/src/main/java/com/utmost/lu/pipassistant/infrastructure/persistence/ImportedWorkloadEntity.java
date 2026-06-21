package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Raw per-team workload of an imported requirement, as found in the file for that version. */
@Entity
@Table(name = "imported_workload")
public class ImportedWorkloadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "imported_requirement_id", nullable = false)
    private Long importedRequirementId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "estimate", precision = 7, scale = 2)
    private BigDecimal estimate;

    protected ImportedWorkloadEntity() {
    }

    public ImportedWorkloadEntity(Long importedRequirementId, Long teamId, BigDecimal estimate) {
        this.importedRequirementId = importedRequirementId;
        this.teamId = teamId;
        this.estimate = estimate;
    }

    public Long getId() {
        return id;
    }

    public Long getImportedRequirementId() {
        return importedRequirementId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public BigDecimal getEstimate() {
        return estimate;
    }
}
