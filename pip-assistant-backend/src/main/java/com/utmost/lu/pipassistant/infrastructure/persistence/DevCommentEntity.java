package com.utmost.lu.pipassistant.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "dev_comment", uniqueConstraints = @UniqueConstraint(columnNames = {"requirement_id", "team_id"}))
public class DevCommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requirement_id", nullable = false)
    private Long requirementId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "text", length = 2000)
    private String text;

    protected DevCommentEntity() {
    }

    public DevCommentEntity(Long requirementId, Long teamId, String text) {
        this.requirementId = requirementId;
        this.teamId = teamId;
        this.text = text;
    }

    public Long getRequirementId() {
        return requirementId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
