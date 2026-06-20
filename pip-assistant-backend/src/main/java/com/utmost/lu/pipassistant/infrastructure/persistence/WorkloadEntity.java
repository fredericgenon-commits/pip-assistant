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
}
