package com.utmost.lu.pipassistant.domain.model;

import java.math.BigDecimal;

/** A team's capacity (story points) for a PIP. */
public record PipCapacity(Long pipId, Long teamId, BigDecimal capacity) {
}
