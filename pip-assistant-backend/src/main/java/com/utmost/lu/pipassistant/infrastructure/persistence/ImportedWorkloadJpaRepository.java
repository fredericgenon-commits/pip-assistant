package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportedWorkloadJpaRepository extends JpaRepository<ImportedWorkloadEntity, Long> {

    List<ImportedWorkloadEntity> findByImportedRequirementIdIn(Collection<Long> importedRequirementIds);
}
