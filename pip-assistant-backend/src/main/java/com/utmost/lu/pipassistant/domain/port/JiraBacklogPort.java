package com.utmost.lu.pipassistant.domain.port;

import java.util.List;

import com.utmost.lu.pipassistant.domain.model.DevTicket;

/** Outbound port for fetching the dev tickets (epic children) of a REQ from JIRA. */
public interface JiraBacklogPort {

    /**
     * Returns all Story-type tickets whose epic link equals {@code reqKey}.
     * Returns an empty list when no tickets are found or the REQ does not exist.
     */
    List<DevTicket> fetchDevTickets(String reqKey);
}
