package com.utmost.lu.pipassistant.domain.model;

import java.math.BigDecimal;

/**
 * The effort (story points) of a team on a requirement. When {@code tbd} is true the team is
 * impacted but the estimate is not known yet ("To Be Defined"); {@code estimate} is then null.
 * When {@code jiraLocked} is true the cell value was written by the JIRA sync and is read-only.
 */
public record Workload(Long requirementId, Long teamId, BigDecimal estimate, boolean tbd, boolean jiraLocked) {
}
