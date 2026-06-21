package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportedRequirementJpaRepository extends JpaRepository<ImportedRequirementEntity, Long> {

    List<ImportedRequirementEntity> findByExcelImportId(Long excelImportId);
}
