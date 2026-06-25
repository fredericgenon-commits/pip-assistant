package com.utmost.lu.pipassistant.domain.port;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.utmost.lu.pipassistant.domain.model.ImportDiff;
import com.utmost.lu.pipassistant.domain.model.SnapshotRequirement;

/**
 * Outbound port for Excel import versioning: version numbering, the previous version's
 * snapshot (diff baseline) and applying a new import (store the snapshot + update the live
 * requirements/workloads, honouring manual workload overrides).
 */
public interface ExcelImportRepository {

    /** Next version number for the PIP (1 for the first import). */
    int nextVersionNo(Long pipId);

    /** Requirements of the PIP's latest stored version, or empty if none. */
    List<SnapshotRequirement> findLatestSnapshot(Long pipId);

    /**
     * Persist a new import version: store the raw snapshot, upsert projects/requirements
     * (priority + PIP status), upsert workloads (skipping manually overridden cells) and
     * mark removed requirements as REMOVED_FROM_PIP.
     */
    void applyImport(Long pipId, int versionNo, String originalFilename, Instant importedAt, ImportDiff diff);

    /** Metadata of the latest import for the PIP, or empty if no import has been made yet. */
    Optional<ImportMeta> findLastImportMeta(Long pipId);

    record ImportMeta(int versionNo, String originalFilename, Instant importedAt) {}
}
