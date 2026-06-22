package com.utmost.lu.pipassistant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.utmost.lu.pipassistant.domain.model.PipCode;
import com.utmost.lu.pipassistant.domain.model.PipStatus;
import com.utmost.lu.pipassistant.domain.model.Requirement;
import com.utmost.lu.pipassistant.domain.model.Team;
import com.utmost.lu.pipassistant.domain.model.Workload;
import com.utmost.lu.pipassistant.domain.model.Pip;
import com.utmost.lu.pipassistant.domain.port.JiraBacklogPort;
import com.utmost.lu.pipassistant.domain.port.JiraPort;
import com.utmost.lu.pipassistant.domain.port.PipDetailRepository;
import com.utmost.lu.pipassistant.domain.port.PipRepository;
import com.utmost.lu.pipassistant.domain.port.RequirementBacklogRepository;
import com.utmost.lu.pipassistant.domain.port.TeamRepository;
import com.utmost.lu.pipassistant.infrastructure.config.JiraProperties;

@ExtendWith(MockitoExtension.class)
class JiraSyncServiceTest {

    @Mock private JiraPort jiraPort;
    @Mock private JiraBacklogPort jiraBacklogPort;
    @Mock private PipDetailRepository detailRepository;
    @Mock private RequirementBacklogRepository backlogRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private PipRepository pipRepository;

    private final JiraProperties jiraProperties = new JiraProperties();

    private JiraSyncService service() {
        return new JiraSyncService(
                jiraPort, jiraBacklogPort, detailRepository, backlogRepository,
                teamRepository, pipRepository, jiraProperties);
    }

    @BeforeEach
    void setUp() {
        jiraProperties.setSyncTtlMinutes(15);
        jiraProperties.setTeamMapping(Map.of("Core", "Core", "Portal", "Portal"));

        lenient().when(pipRepository.findById(1L))
                .thenReturn(Optional.of(new Pip(1L, PipCode.of("26_PIP_1"), null, null, PipStatus.PREPARATION)));
        lenient().when(teamRepository.findAllOrdered())
                .thenReturn(List.of(new Team(10L, "Core"), new Team(11L, "Portal")));
        lenient().when(detailRepository.findRequirementsByPip(1L))
                .thenReturn(List.of(new Requirement(7L, "REQ-1", "desc", "TODO", "pm", 5L, 1, "NEW")));
        lenient().when(detailRepository.findWorkloadsByPip(1L)).thenReturn(List.of());
        lenient().when(jiraPort.fetchStatus(anyString())).thenReturn(Optional.of("In Progress"));
        lenient().when(jiraBacklogPort.fetchDevTickets(anyString())).thenReturn(List.of());
    }

    @Test
    void sync_throwsWhenPipNotFound() {
        when(pipRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(PipNotFoundException.class)
                .isThrownBy(() -> service().sync(99L));
    }

    @Test
    void sync_callsJiraPortForEachRequirement() {
        JiraSyncResult result = service().sync(1L);

        verify(jiraPort).fetchStatus("REQ-1");
        verify(jiraBacklogPort).fetchDevTickets("REQ-1");
        assertThat(result.synced()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(0);
    }

    @Test
    void sync_updatesRequirementStatusFromJira() {
        when(jiraPort.fetchStatus("REQ-1")).thenReturn(Optional.of("Done"));

        service().sync(1L);

        verify(detailRepository).updateRequirementStatus(7L, "Done");
    }

    @Test
    void sync_upsertsWorkloadFromJiraWhenSpIsPositive() {
        // One RFI ticket for Core (teamId=10) with 5 SP
        when(jiraBacklogPort.fetchDevTickets("REQ-1")).thenReturn(List.of(
                new com.utmost.lu.pipassistant.domain.model.DevTicket(
                        "DEV-1", "DEV", "Ready for implementation", "Project", "Core", 5)));

        service().sync(1L);

        verify(detailRepository).upsertWorkloadFromJira(7L, 10L, BigDecimal.valueOf(5));
    }

    @Test
    void sync_unlocksWorkloadWhenSpBecomesZero() {
        // The Core cell was previously locked by JIRA.
        when(detailRepository.findWorkloadsByPip(1L))
                .thenReturn(List.of(new Workload(7L, 10L, new BigDecimal("5"), false, true)));
        // Now JIRA has no RFI tickets for Core.
        when(jiraBacklogPort.fetchDevTickets("REQ-1")).thenReturn(List.of());

        service().sync(1L);

        verify(detailRepository).unlockWorkloadFromJira(7L, 10L);
        verify(detailRepository, never()).upsertWorkloadFromJira(anyLong(), anyLong(), any());
    }

    @Test
    void sync_skipsJiraWhenSyncedRecently() {
        JiraSyncService svc = service();
        svc.sync(1L); // first call — hits JIRA
        svc.sync(1L); // second call — TTL not expired, should skip

        verify(jiraPort, times(1)).fetchStatus(anyString());
    }

    @Test
    void sync_upsertsTeamStatusForAllTeams() {
        service().sync(1L);

        // Both teams (Core=10, Portal=11) should get a team status upsert (null in this case).
        verify(backlogRepository).upsertTeamStatus(eq(7L), eq(10L), any());
        verify(backlogRepository).upsertTeamStatus(eq(7L), eq(11L), any());
    }
}
