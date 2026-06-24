package com.utmost.lu.pipassistant.domain.model;

/**
 * A JIRA Story ticket that is a child of a REQ epic, as returned by {@code JiraBacklogPort}.
 *
 * @param key            JIRA issue key (e.g. "DEV-512")
 * @param summary        issue summary (used to detect Technical Analysis tickets)
 * @param status         JIRA status name (e.g. "Ready for implementation", "Open")
 * @param deliveryMethod value of the delivery-method custom field (e.g. "Project")
 * @param jiraTeam       value of the team custom field (raw JIRA value, mapped via config)
 * @param storyPoints    story points (null if not set)
 */
public record DevTicket(
        String key,
        String summary,
        String status,
        String deliveryMethod,
        String jiraTeam,
        Integer storyPoints) {
}
