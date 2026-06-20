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
@Table(name = "pip_capacity", uniqueConstraints = @UniqueConstraint(columnNames = {"pip_id", "team_id"}))
public class PipCapacityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pip_id", nullable = false)
    private Long pipId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "capacity", precision = 9, scale = 2)
    private BigDecimal capacity;

    protected PipCapacityEntity() {
    }

    public PipCapacityEntity(Long pipId, Long teamId, BigDecimal capacity) {
        this.pipId = pipId;
        this.teamId = teamId;
        this.capacity = capacity;
    }

    public Long getPipId() {
        return pipId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public BigDecimal getCapacity() {
        return capacity;
    }

    public void setCapacity(BigDecimal capacity) {
        this.capacity = capacity;
    }
}
