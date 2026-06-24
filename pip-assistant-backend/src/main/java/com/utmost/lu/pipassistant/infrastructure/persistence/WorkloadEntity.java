package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "workload", uniqueConstraints = @UniqueConstraint(columnNames = {"requirement_id", "team_id"}))
public class WorkloadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requirement_id", nullable = false)
    private Long requirementId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "estimate", precision = 7, scale = 2)
    private BigDecimal estimate;

    /** True when the team is impacted but the estimate is not known yet (estimate is null). */
    @Column(name = "tbd", nullable = false)
    private boolean tbd = false;

    /** True once a user edits this cell: subsequent imports must not overwrite it. */
    @Column(name = "manual_override", nullable = false)
    private boolean manualOverride = false;

    /** True when the cell value was written by the JIRA backlog sync (read-only in the UI). */
    @Column(name = "jira_locked", nullable = false)
    private boolean jiraLocked = false;

    protected WorkloadEntity() {
    }

    public WorkloadEntity(Long requirementId, Long teamId, BigDecimal estimate) {
        this.requirementId = requirementId;
        this.teamId = teamId;
        this.estimate = estimate;
    }

    public Long getRequirementId() {
        return requirementId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public BigDecimal getEstimate() {
        return estimate;
    }

    public void setEstimate(BigDecimal estimate) {
        this.estimate = estimate;
    }

    public boolean isTbd() {
        return tbd;
    }

    public void setTbd(boolean tbd) {
        this.tbd = tbd;
    }

    public boolean isManualOverride() {
        return manualOverride;
    }

    public void setManualOverride(boolean manualOverride) {
        this.manualOverride = manualOverride;
    }

    public boolean isJiraLocked() {
        return jiraLocked;
    }

    public void setJiraLocked(boolean jiraLocked) {
        this.jiraLocked = jiraLocked;
    }
}
