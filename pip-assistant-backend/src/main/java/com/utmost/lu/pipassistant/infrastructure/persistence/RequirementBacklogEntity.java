package com.utmost.lu.pipassistant.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "requirement_backlog",
        uniqueConstraints = @UniqueConstraint(columnNames = {"requirement_id", "team_id"}))
public class RequirementBacklogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requirement_id", nullable = false)
    private Long requirementId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "team_status", length = 50)
    private String teamStatus;

    protected RequirementBacklogEntity() {}

    public RequirementBacklogEntity(Long requirementId, Long teamId, String teamStatus) {
        this.requirementId = requirementId;
        this.teamId = teamId;
        this.teamStatus = teamStatus;
    }

    public Long getId() { return id; }
    public Long getRequirementId() { return requirementId; }
    public Long getTeamId() { return teamId; }
    public String getTeamStatus() { return teamStatus; }
    public void setTeamStatus(String teamStatus) { this.teamStatus = teamStatus; }
}
