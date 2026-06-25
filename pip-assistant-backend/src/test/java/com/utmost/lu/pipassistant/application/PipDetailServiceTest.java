package com.utmost.lu.pipassistant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.utmost.lu.pipassistant.domain.model.DevComment;
import com.utmost.lu.pipassistant.domain.model.Pip;
import com.utmost.lu.pipassistant.domain.model.PipCapacity;
import com.utmost.lu.pipassistant.domain.model.PipCode;
import com.utmost.lu.pipassistant.domain.model.PipStatus;
import com.utmost.lu.pipassistant.domain.model.Project;
import com.utmost.lu.pipassistant.domain.model.Requirement;
import com.utmost.lu.pipassistant.domain.model.Team;
import com.utmost.lu.pipassistant.domain.model.Workload;
import com.utmost.lu.pipassistant.domain.port.ExcelImportRepository;
import com.utmost.lu.pipassistant.domain.port.PipDetailRepository;
import com.utmost.lu.pipassistant.domain.port.PipRepository;
import com.utmost.lu.pipassistant.domain.port.RequirementBacklogRepository;
import com.utmost.lu.pipassistant.domain.port.TeamRepository;

@ExtendWith(MockitoExtension.class)
class PipDetailServiceTest {

    @Mock private PipRepository pipRepository;
    @Mock private PipDetailRepository detailRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private RequirementStatusCatalog statusCatalog;
    @Mock private RequirementBacklogRepository backlogRepository;
    @Mock private ExcelImportRepository importRepository;

    private PipDetailService service() {
        return new PipDetailService(pipRepository, detailRepository, teamRepository, statusCatalog,
                backlogRepository, importRepository);
    }

    private static Pip pip(long id) {
        return new Pip(id, PipCode.of("26_PIP_1"), null, null, PipStatus.PREPARATION);
    }

    @Test
    void getDetail_assemblesRowsWithProjectAndMaps() {
        when(pipRepository.findById(1L)).thenReturn(Optional.of(pip(1L)));
        when(teamRepository.findAllOrdered()).thenReturn(List.of(new Team(10L, "Core")));
        when(detailRepository.findProjectsByPip(1L))
                .thenReturn(List.of(new Project(5L, "TCM-1", "TCM desc", 1L)));
        when(detailRepository.findRequirementsByPip(1L))
                .thenReturn(List.of(new Requirement(7L, "REQ-1", "req desc", "TODO", "pm", 5L, 1, "NEW")));
        when(detailRepository.findWorkloadsByPip(1L))
                .thenReturn(List.of(new Workload(7L, 10L, new BigDecimal("3.5"), false, false)));
        when(detailRepository.findDevCommentsByPip(1L))
                .thenReturn(List.of(new DevComment(7L, 10L, "dev note")));
        when(detailRepository.findCapacitiesByPip(1L))
                .thenReturn(List.of(new PipCapacity(1L, 10L, new BigDecimal("20"))));
        when(backlogRepository.findByPip(1L)).thenReturn(List.of());
        when(importRepository.findLastImportMeta(1L)).thenReturn(Optional.empty());

        PipDetailView view = service().getDetail(1L);

        assertThat(view.requirements()).hasSize(1);
        PipDetailView.RequirementRow row = view.requirements().get(0);
        assertThat(row.tcmKey()).isEqualTo("TCM-1");
        assertThat(row.tcmDescription()).isEqualTo("TCM desc");
        assertThat(row.workloads()).containsEntry(10L, "3.5");
        assertThat(row.comments()).containsEntry(10L, "dev note");
        assertThat(view.capacities()).containsEntry(10L, new BigDecimal("20"));
    }

    @Test
    void getDetail_throwsWhenPipMissing() {
        when(pipRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(PipNotFoundException.class)
                .isThrownBy(() -> service().getDetail(99L));
    }

    @Test
    void save_appliesEditsAndCapacities() {
        when(pipRepository.findById(1L)).thenReturn(Optional.of(pip(1L)));
        when(statusCatalog.all()).thenReturn(List.of("TODO", "IN_PROGRESS", "DONE"));
        when(detailRepository.findRequirementsByPip(1L))
                .thenReturn(List.of(new Requirement(7L, "REQ-1", "d", "TODO", "pm", 5L, 1, "NEW")));

        var edit = new SavePipDetailCommand.RequirementEdit(
                7L, "new tcm desc", "new req desc", "DONE", "pm2",
                Map.of(10L, "4"), Map.of(10L, "note"));
        service().save(1L, new SavePipDetailCommand(List.of(edit), Map.of(10L, new BigDecimal("15"))));

        verify(detailRepository).updateRequirement(7L, "new req desc", "DONE", "pm2");
        verify(detailRepository).updateProjectDescription(5L, "new tcm desc");
        verify(detailRepository).upsertWorkload(7L, 10L, new BigDecimal("4"), false);
        verify(detailRepository).upsertDevComment(7L, 10L, "note");
        verify(detailRepository).upsertCapacity(1L, 10L, new BigDecimal("15"));
    }

    @Test
    void save_parsesTbdWorkloadCell() {
        when(pipRepository.findById(1L)).thenReturn(Optional.of(pip(1L)));
        when(statusCatalog.all()).thenReturn(List.of("TODO", "IN_PROGRESS", "DONE"));
        when(detailRepository.findRequirementsByPip(1L))
                .thenReturn(List.of(new Requirement(7L, "REQ-1", "d", "TODO", "pm", 5L, 1, "NEW")));

        var edit = new SavePipDetailCommand.RequirementEdit(
                7L, "t", "d", "TODO", "pm", Map.of(10L, "TBD"), Map.of());
        service().save(1L, new SavePipDetailCommand(List.of(edit), Map.of()));

        verify(detailRepository).upsertWorkload(7L, 10L, null, true);
    }

    @Test
    void save_rejectsUnknownStatus_andPersistsNothing() {
        when(pipRepository.findById(1L)).thenReturn(Optional.of(pip(1L)));
        when(statusCatalog.all()).thenReturn(List.of("TODO", "IN_PROGRESS", "DONE"));

        var edit = new SavePipDetailCommand.RequirementEdit(
                7L, null, "d", "WRONG", null, Map.of(), Map.of());

        assertThatExceptionOfType(InvalidRequirementStatusException.class).isThrownBy(
                () -> service().save(1L, new SavePipDetailCommand(List.of(edit), Map.of())));

        verify(detailRepository, never()).updateRequirement(any(), any(), any(), any());
        verify(detailRepository, never()).upsertCapacity(eq(1L), any(), any());
    }
}
