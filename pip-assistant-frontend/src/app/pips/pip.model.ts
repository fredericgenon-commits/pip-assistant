export type PipStatus = 'PREPARATION' | 'ACTIVE' | 'CLOSED';

export interface Pip {
  id: number;
  code: string;
  status: PipStatus;
}

export interface NextCode {
  code: string;
}
