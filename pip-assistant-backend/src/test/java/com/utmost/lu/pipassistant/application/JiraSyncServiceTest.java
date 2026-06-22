package com.utmost.lu.pipassistant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.utmost.lu.pipassistant.domain.model.Pip;
import com.utmost.lu.pipassistant.domain.model.PipCode;
import com.utmost.lu.pipassistant.domain.model.PipStatus;
import com.utmost.lu.pipassistant.domain.model.Requirement;
import com.utmost.lu.pipassistant.domain.port.JiraPort;
import com.utmost.lu.pipassistant.domain.port.PipDetailRepository;
import com.utmost.lu.pipassistant.domain.port.PipRepository;

@ExtendWith(MockitoExtension.class)
class JiraSyncServiceTest {

    @Mock private JiraPort jiraPort;
    @Mock private PipDetailRepository detailRepository;
    @Mock private PipRepository pipRepository;

    private JiraSyncService service() {
        return new JiraSyncService(jiraPort, detailRepository, pipRepository);
    }

    private static Pip pip(long id) {
        return new Pip(id, PipCode.of("26_PIP_1"), null, null, PipStatus.PREPARATION);
    }

    private static Requirement req(long id, String key) {
        return new Requirement(id, key, "desc", null, null, 5L, 1, "NEW");
    }

    @Test
    void sync_updatesAllRequirements() {
        when(pipRepository.findById(1L)).thenReturn(Optional.of(pip(1L)));
        when(detailRepository.findRequirementsByPip(1L))
                .thenReturn(List.of(req(7L, "REQ-1"), req(8L, "REQ-2")));
        when(jiraPort.fetchStatus("REQ-1")).thenReturn(Optional.of("In Progress"));
        when(jiraPort.fetchStatus("REQ-2")).thenReturn(Optional.of("Done"));

        JiraSyncResult result = service().sync(1L);

        assertThat(result.synced()).isEqualTo(2);
        assertThat(result.failed()).isEqualTo(0);
        assertThat(result.errors()).isEmpty();
        verify(detailRepository).updateRequirementStatus(7L, "In Progress");
        verify(detailRepository).updateRequirementStatus(8L, "Done");
    }

    @Test
    void sync_countsNotFoundAsFailure() {
        when(pipRepository.findById(1L)).thenReturn(Optional.of(pip(1L)));
        when(detailRepository.findRequirementsByPip(1L)).thenReturn(List.of(req(7L, "REQ-1")));
        when(jiraPort.fetchStatus("REQ-1")).thenReturn(Optional.empty());

        JiraSyncResult result = service().sync(1L);

        assertThat(result.synced()).isEqualTo(0);
        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.errors()).hasSize(1);
        verify(detailRepository, never()).updateRequirementStatus(any(), any());
    }

    @Test
    void sync_toleratesPartialFailure() {
        when(pipRepository.findById(1L)).thenReturn(Optional.of(pip(1L)));
        when(detailRepository.findRequirementsByPip(1L))
                .thenReturn(List.of(req(7L, "REQ-1"), req(8L, "REQ-2")));
        when(jiraPort.fetchStatus("REQ-1")).thenReturn(Optional.of("In Progress"));
        when(jiraPort.fetchStatus("REQ-2")).thenThrow(new RuntimeException("connection refused"));

        JiraSyncResult result = service().sync(1L);

        assertThat(result.synced()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.errors()).hasSize(1);
        verify(detailRepository).updateRequirementStatus(7L, "In Progress");
        verify(detailRepository, never()).updateRequirementStatus(eq(8L), any());
    }

    @Test
    void sync_throwsWhenPipMissing() {
        when(pipRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(PipNotFoundException.class)
                .isThrownBy(() -> service().sync(99L));
    }

    @Test
    void sync_returnsEmptyResultForPipWithNoRequirements() {
        when(pipRepository.findById(1L)).thenReturn(Optional.of(pip(1L)));
        when(detailRepository.findRequirementsByPip(1L)).thenReturn(List.of());

        JiraSyncResult result = service().sync(1L);

        assertThat(result.synced()).isEqualTo(0);
        assertThat(result.failed()).isEqualTo(0);
    }
}
