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
  status: string;
  pmComment: string;
  /** 1-based import priority; null when removed from the PIP. */
  priority: number | null;
  /** Diff-derived PIP status name (NEW, CHANGED, REMOVED_FROM_PIP, ...); null before any import. */
  pipStatus: string | null;
  /** team id -> story points */
  workloads: Record<number, number | null>;
  /** team id -> dev comment */
  comments: Record<number, string>;
}

export interface PipDetail {
  pip: PipInfo;
  teams: TeamRef[];
  requirements: RequirementRow[];
  /** team id -> capacity */
  capacities: Record<number, number | null>;
}

export interface SavePipDetailPayload {
  requirements: Array<{
    id: number;
    tcmDescription: string;
    description: string;
    status: string;
    pmComment: string;
    workloads: Record<number, number | null>;
    comments: Record<number, string>;
  }>;
  capacities: Record<number, number | null>;
}
