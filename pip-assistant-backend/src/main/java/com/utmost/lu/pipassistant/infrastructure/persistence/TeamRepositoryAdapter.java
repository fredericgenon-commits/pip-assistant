package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.utmost.lu.pipassistant.domain.model.Team;
import com.utmost.lu.pipassistant.domain.port.TeamRepository;

@Component
public class TeamRepositoryAdapter implements TeamRepository {

    private final TeamJpaRepository jpaRepository;

    public TeamRepositoryAdapter(TeamJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Team> findAllOrdered() {
        return jpaRepository.findAllByOrderByIdAsc().stream()
                .map(e -> new Team(e.getId(), e.getName()))
                .toList();
    }
}
