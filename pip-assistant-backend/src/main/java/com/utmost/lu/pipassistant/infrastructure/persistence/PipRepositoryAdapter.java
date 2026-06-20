package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.utmost.lu.pipassistant.domain.model.Pip;
import com.utmost.lu.pipassistant.domain.model.PipCode;
import com.utmost.lu.pipassistant.domain.port.PipRepository;

/**
 * Adapter mapping the {@link PipRepository} port onto Spring Data JPA.
 */
@Component
public class PipRepositoryAdapter implements PipRepository {

    private final PipJpaRepository jpaRepository;

    public PipRepositoryAdapter(PipJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Pip> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Pip> findByYear(int twoDigitYear) {
        String prefix = "%02d_PIP_".formatted(twoDigitYear);
        return jpaRepository.findByCodeStartingWith(prefix).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByCode(PipCode code) {
        return jpaRepository.existsByCode(code.value());
    }

    @Override
    public Optional<Pip> findMax() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .max(Comparator.comparing(Pip::code));
    }

    @Override
    public Optional<Pip> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Pip save(Pip pip) {
        PipEntity saved = jpaRepository.save(toEntity(pip));
        return toDomain(saved);
    }

    private Pip toDomain(PipEntity entity) {
        return new Pip(
                entity.getId(),
                PipCode.of(entity.getCode()),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getStatus());
    }

    private PipEntity toEntity(Pip pip) {
        return new PipEntity(
                pip.id(),
                pip.code().value(),
                pip.startDate(),
                pip.endDate(),
                pip.status());
    }
}
