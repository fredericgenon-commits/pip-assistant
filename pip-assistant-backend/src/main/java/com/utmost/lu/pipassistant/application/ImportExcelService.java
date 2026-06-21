package com.utmost.lu.pipassistant.application;

import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.utmost.lu.pipassistant.domain.model.ImportDiff;
import com.utmost.lu.pipassistant.domain.model.ParsedRequirement;
import com.utmost.lu.pipassistant.domain.model.SnapshotRequirement;
import com.utmost.lu.pipassistant.domain.port.ExcelImportRepository;
import com.utmost.lu.pipassistant.domain.port.ExcelRequirementParser;
import com.utmost.lu.pipassistant.domain.port.PipRepository;
import com.utmost.lu.pipassistant.domain.service.ImportDiffCalculator;

/**
 * Orchestrates an Excel import: parse the file, diff it against the previous version's
 * snapshot, persist the new version and return the refreshed PIP detail. All business
 * logic (parsing, diff, priority, status, override) lives here / in the domain.
 */
@Service
public class ImportExcelService {

    private final PipRepository pipRepository;
    private final ExcelRequirementParser parser;
    private final ExcelImportRepository importRepository;
    private final PipDetailService pipDetailService;
    private final Clock clock;
    private final ImportDiffCalculator diffCalculator = new ImportDiffCalculator();

    public ImportExcelService(
            PipRepository pipRepository,
            ExcelRequirementParser parser,
            ExcelImportRepository importRepository,
            PipDetailService pipDetailService,
            Clock clock) {
        this.pipRepository = pipRepository;
        this.parser = parser;
        this.importRepository = importRepository;
        this.pipDetailService = pipDetailService;
        this.clock = clock;
    }

    /** Import a planning file into the PIP and return the refreshed detail view. */
    @Transactional
    public PipDetailView importFile(Long pipId, String originalFilename, InputStream content) {
        pipRepository.findById(pipId).orElseThrow(() -> new PipNotFoundException(pipId));

        List<ParsedRequirement> parsed = parser.parse(content);
        List<SnapshotRequirement> previous = importRepository.findLatestSnapshot(pipId);
        ImportDiff diff = diffCalculator.diff(parsed, previous);

        int versionNo = importRepository.nextVersionNo(pipId);
        importRepository.applyImport(pipId, versionNo, originalFilename, Instant.now(clock), diff);

        return pipDetailService.getDetail(pipId);
    }
}
