package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExcelImportJpaRepository extends JpaRepository<ExcelImportEntity, Long> {

    Optional<ExcelImportEntity> findFirstByPipIdOrderByVersionNoDesc(Long pipId);
}
