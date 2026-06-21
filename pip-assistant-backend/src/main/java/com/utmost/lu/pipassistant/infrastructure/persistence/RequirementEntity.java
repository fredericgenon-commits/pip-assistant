package com.utmost.lu.pipassistant.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "requirement")
public class RequirementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "req_key", nullable = false, unique = true, length = 32)
    private String reqKey;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "pm_comment", length = 2000)
    private String pmComment;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /** 1-based order of appearance in the latest import; null when removed from the PIP. */
    @Column(name = "priority")
    private Integer priority;

    /** Diff-derived PIP status (NEW, UNCHANGED, CHANGED, PRIORITY_CHANGED, REMOVED_FROM_PIP, MISSING_DATA). */
    @Column(name = "pip_status", length = 40)
    private String pipStatus;

    protected RequirementEntity() {
    }

    public RequirementEntity(Long id, String reqKey, String description, String status,
                             String pmComment, Long projectId) {
        this.id = id;
        this.reqKey = reqKey;
        this.description = description;
        this.status = status;
        this.pmComment = pmComment;
        this.projectId = projectId;
    }

    public Long getId() {
        return id;
    }

    public String getReqKey() {
        return reqKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPmComment() {
        return pmComment;
    }

    public void setPmComment(String pmComment) {
        this.pmComment = pmComment;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getPipStatus() {
        return pipStatus;
    }

    public void setPipStatus(String pipStatus) {
        this.pipStatus = pipStatus;
    }
}
