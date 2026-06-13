// TypeScript mirrors of the backend view/DTO records (see domain.service.dto).

export type Choice = "GREEN" | "RED";
export type ExecutionStatus = "RUNNING" | "SUCCESS" | "FAILED";
export type Role = "USER" | "ADMIN";

export interface Site {
  id: number;
  name: string;
  baseUrl: string;
  loginUrl: string;
  selectors: Record<string, string>;
  createdBy: number | null;
  createdAt: string;
}

export interface Credential {
  id: number;
  ownerUserId: number;
  siteId: number;
  siteName: string;
  label: string | null;
  username: string;
  secretRef: string;
  createdAt: string;
}

export interface Schedule {
  id: number;
  credentialId: number;
  intervalSeconds: number;
  actionStrategy: string;
  waitBeforeLogoutMs: number;
  enabled: boolean;
  eventbridgeScheduleArn: string | null;
  createdAt: string;
}

export interface Execution {
  id: number;
  scheduleId: number;
  status: ExecutionStatus;
  startedAt: string;
  endedAt: string | null;
  errorMessage: string | null;
  screenshotS3Key: string | null;
}

export interface Action {
  id: number;
  executionId: number;
  actionTime: string;
  choice: Choice;
  pageUrl: string | null;
  durationMs: number | null;
}

export interface ChoiceStats {
  from: string;
  to: string;
  green: number;
  red: number;
}

export interface AppUser {
  id: number;
  email: string;
  displayName: string | null;
  role: Role;
  enabled: boolean;
}

// ---- request payloads ----

export interface CreateSiteRequest {
  name: string;
  baseUrl: string;
  loginUrl: string;
  selectors: Record<string, string>;
}

export interface CreateCredentialRequest {
  siteId: number;
  label?: string;
  username: string;
  password: string;
}

export interface CreateScheduleRequest {
  credentialId: number;
  intervalSeconds: number;
  actionStrategy?: string;
  waitBeforeLogoutMs: number;
}

export interface UpdateScheduleRequest {
  intervalSeconds?: number;
  actionStrategy?: string;
  waitBeforeLogoutMs?: number;
  enabled?: boolean;
}
