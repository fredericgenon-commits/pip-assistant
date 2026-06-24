package com.utmost.lu.pipassistant.application;

import java.util.List;

/** Result of a JIRA synchronisation run for one PIP. */
public record JiraSyncResult(int synced, int failed, List<String> errors) {}
