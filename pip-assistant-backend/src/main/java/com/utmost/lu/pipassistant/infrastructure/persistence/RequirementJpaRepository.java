package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementJpaRepository extends JpaRepository<RequirementEntity, Long> {

    List<RequirementEntity> findByProjectIdIn(Collection<Long> projectIds);
}
