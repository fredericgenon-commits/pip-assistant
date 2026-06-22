package com.utmost.lu.pipassistant.application;

import java.util.List;

/**
 * Outcome of a {@link JiraSyncService#sync(Long)} call.
 *
 * @param synced number of requirements successfully synced
 * @param failed number of requirements that produced errors
 * @param errors per-requirement error messages (format: "REQ-xxx: message")
 */
public record JiraSyncResult(int synced, int failed, List<String> errors) {
}
