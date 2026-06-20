package com.utmost.lu.pipassistant.domain.model;

import java.math.BigDecimal;

/** The effort (story points) of a team on a requirement. */
public record Workload(Long requirementId, Long teamId, BigDecimal estimate) {
}
