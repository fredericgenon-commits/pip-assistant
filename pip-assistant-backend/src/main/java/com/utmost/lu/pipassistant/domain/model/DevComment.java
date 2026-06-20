package com.utmost.lu.pipassistant.domain.model;

/** A development team's comment on a requirement (the "Dev comment" column). */
public record DevComment(Long requirementId, Long teamId, String text) {
}
