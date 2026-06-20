package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link PipEntity}. The 2-digit year is matched on the
 * {@code yy_PIP_} code prefix.
 */
public interface PipJpaRepository extends JpaRepository<PipEntity, Long> {

    boolean existsByCode(String code);

    List<PipEntity> findByCodeStartingWith(String prefix);
}
