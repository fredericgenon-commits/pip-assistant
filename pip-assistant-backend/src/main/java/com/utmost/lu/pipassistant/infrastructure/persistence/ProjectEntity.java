package com.utmost.lu.pipassistant.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "project")
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tcm_key", nullable = false, length = 32)
    private String tcmKey;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "pip_id", nullable = false)
    private Long pipId;

    protected ProjectEntity() {
    }

    public ProjectEntity(Long id, String tcmKey, String description, Long pipId) {
        this.id = id;
        this.tcmKey = tcmKey;
        this.description = description;
        this.pipId = pipId;
    }

    public Long getId() {
        return id;
    }

    public String getTcmKey() {
        return tcmKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPipId() {
        return pipId;
    }
}
