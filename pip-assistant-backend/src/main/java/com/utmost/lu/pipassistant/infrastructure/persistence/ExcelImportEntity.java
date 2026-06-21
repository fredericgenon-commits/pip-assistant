package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** One file deposit (version) for a PIP. */
@Entity
@Table(name = "excel_import")
public class ExcelImportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pip_id", nullable = false)
    private Long pipId;

    @Column(name = "version_no", nullable = false)
    private int versionNo;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "imported_at", nullable = false)
    private Instant importedAt;

    protected ExcelImportEntity() {
    }

    public ExcelImportEntity(Long pipId, int versionNo, String originalFilename, Instant importedAt) {
        this.pipId = pipId;
        this.versionNo = versionNo;
        this.originalFilename = originalFilename;
        this.importedAt = importedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getPipId() {
        return pipId;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public Instant getImportedAt() {
        return importedAt;
    }
}
