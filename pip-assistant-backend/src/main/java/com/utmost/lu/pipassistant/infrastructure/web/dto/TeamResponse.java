package com.utmost.lu.pipassistant.infrastructure.web.dto;

import com.utmost.lu.pipassistant.domain.model.Team;

public record TeamResponse(Long id, String name) {

    public static TeamResponse from(Team team) {
        return new TeamResponse(team.id(), team.name());
    }
}
