package com.utmost.lu.pipassistant.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Raw parsed values of a REQ for one import version (basis for the diff). */
@Entity
@Table(name = "imported_requirement")
public class ImportedRequirementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "excel_import_id", nullable = false)
    private Long excelImportId;

    @Column(name = "req_key", nullable = false, length = 32)
    private String reqKey;

    @Column(name = "tcm_key", length = 32)
    private String tcmKey;

    @Column(name = "tcm_description", length = 1000)
    private String tcmDescription;

    @Column(name = "req_description", length = 1000)
    private String reqDescription;

    @Column(name = "pm_comment", length = 2000)
    private String pmComment;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "missing_data", nullable = false)
    private boolean missingData = false;

    protected ImportedRequirementEntity() {
    }

    public ImportedRequirementEntity(Long excelImportId, String reqKey, String tcmKey,
                                     String tcmDescription, String reqDescription, String pmComment,
                                     Integer priority, boolean missingData) {
        this.excelImportId = excelImportId;
        this.reqKey = reqKey;
        this.tcmKey = tcmKey;
        this.tcmDescription = tcmDescription;
        this.reqDescription = reqDescription;
        this.pmComment = pmComment;
        this.priority = priority;
        this.missingData = missingData;
    }

    public Long getId() {
        return id;
    }

    public Long getExcelImportId() {
        return excelImportId;
    }

    public String getReqKey() {
        return reqKey;
    }

    public String getTcmKey() {
        return tcmKey;
    }

    public String getTcmDescription() {
        return tcmDescription;
    }

    public String getReqDescription() {
        return reqDescription;
    }

    public String getPmComment() {
        return pmComment;
    }

    public Integer getPriority() {
        return priority;
    }

    public boolean isMissingData() {
        return missingData;
    }
}
