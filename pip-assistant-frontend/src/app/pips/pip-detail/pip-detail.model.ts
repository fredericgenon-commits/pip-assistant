export interface TeamRef {
  id: number;
  name: string;
}

export interface PipInfo {
  id: number;
  code: string;
  status: string;
}

export interface RequirementRow {
  id: number;
  tcmKey: string;
  tcmDescription: string;
  reqKey: string;
  description: string;
  status: string | null;
  pmComment: string;
  /** 1-based import priority; null when removed from the PIP. */
  priority: number | null;
  /** Diff-derived PIP status name (NEW, CHANGED, REMOVED_FROM_PIP, ...); null before any import. */
  pipStatus: string | null;
  /** team id -> workload cell text: a number, "TBD", or empty. */
  workloads: Record<number, string>;
  /** team id -> dev comment */
  comments: Record<number, string>;
  /** JIRA deep-link for the REQ ticket; null if JIRA is not configured. */
  reqUrl: string | null;
  /** JIRA deep-link for the TCM ticket; null if JIRA is not configured. */
  tcmUrl: string | null;
}

export interface PipDetail {
  pip: PipInfo;
  teams: TeamRef[];
  requirements: RequirementRow[];
  /** team id -> capacity */
  capacities: Record<number, number | null>;
}

export interface JiraSyncResult {
  synced: number;
  failed: number;
  errors: string[];
}

export interface JiraSyncSettings {
  interactionThresholdSeconds: number;
}

export interface SavePipDetailPayload {
  requirements: Array<{
    id: number;
    tcmDescription: string;
    description: string;
    pmComment: string;
    workloads: Record<number, string>;
    comments: Record<number, string>;
  }>;
  capacities: Record<number, number | null>;
}
