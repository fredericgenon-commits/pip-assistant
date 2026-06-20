package com.utmost.lu.pipassistant.domain.port;

import java.util.List;

import com.utmost.lu.pipassistant.domain.model.Team;

/** Outbound port for team reference data. */
public interface TeamRepository {

    /** All teams, in their fixed display order. */
    List<Team> findAllOrdered();
}
