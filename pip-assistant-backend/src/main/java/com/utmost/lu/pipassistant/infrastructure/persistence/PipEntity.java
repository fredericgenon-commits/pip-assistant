package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.time.LocalDate;

import com.utmost.lu.pipassistant.domain.model.PipStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA mapping of the {@code pip} table. Schema is owned by Flyway; this entity only maps it.
 */
@Entity
@Table(name = "pip")
public class PipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 32)
    private String code;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PipStatus status;

    protected PipEntity() {
        // for JPA
    }

    public PipEntity(Long id, String code, LocalDate startDate, LocalDate endDate, PipStatus status) {
        this.id = id;
        this.code = code;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public PipStatus getStatus() {
        return status;
    }
}
