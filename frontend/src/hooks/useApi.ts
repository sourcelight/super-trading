import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type {
  Action,
  AppUser,
  ChoiceStats,
  Credential,
  CreateCredentialRequest,
  CreateScheduleRequest,
  CreateSiteRequest,
  Execution,
  Schedule,
  Site,
  UpdateScheduleRequest,
} from "@/types/api";

// ---- Sites ----

export function useSites() {
  return useQuery({
    queryKey: ["sites"],
    queryFn: async () => (await api.get<Site[]>("/api/sites")).data,
  });
}

export function useCreateSite() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (body: CreateSiteRequest) =>
      (await api.post<Site>("/api/sites", body)).data,
    onSuccess: () => qc.invalidateQueries({ queryKey: ["sites"] }),
  });
}

// ---- Credentials ----

export function useCredentials() {
  return useQuery({
    queryKey: ["credentials"],
    queryFn: async () => (await api.get<Credential[]>("/api/credentials")).data,
  });
}

export function useCreateCredential() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (body: CreateCredentialRequest) =>
      (await api.post<Credential>("/api/credentials", body)).data,
    onSuccess: () => qc.invalidateQueries({ queryKey: ["credentials"] }),
  });
}

// ---- Schedules ----

export function useSchedules() {
  return useQuery({
    queryKey: ["schedules"],
    queryFn: async () => (await api.get<Schedule[]>("/api/schedules")).data,
  });
}

export function useCreateSchedule() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (body: CreateScheduleRequest) =>
      (await api.post<Schedule>("/api/schedules", body)).data,
    onSuccess: () => qc.invalidateQueries({ queryKey: ["schedules"] }),
  });
}

export function useUpdateSchedule() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, body }: { id: number; body: UpdateScheduleRequest }) =>
      (await api.put<Schedule>(`/api/schedules/${id}`, body)).data,
    onSuccess: () => qc.invalidateQueries({ queryKey: ["schedules"] }),
  });
}

export function useDeleteSchedule() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/api/schedules/${id}`);
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ["schedules"] }),
  });
}

// ---- Executions & actions ----

export function useExecutions(scheduleId: number | null, from?: string, to?: string) {
  return useQuery({
    queryKey: ["executions", scheduleId, from, to],
    enabled: scheduleId != null,
    queryFn: async () =>
      (
        await api.get<Execution[]>("/api/executions", {
          params: { scheduleId, from, to },
        })
      ).data,
  });
}

export function useActions(executionId: number | null) {
  return useQuery({
    queryKey: ["actions", executionId],
    enabled: executionId != null,
    queryFn: async () =>
      (await api.get<Action[]>(`/api/executions/${executionId}/actions`)).data,
  });
}

// ---- Stats ----

export function useChoiceStats(from?: string, to?: string) {
  return useQuery({
    queryKey: ["stats", "choices", from, to],
    queryFn: async () =>
      (await api.get<ChoiceStats>("/api/stats/choices", { params: { from, to } })).data,
  });
}

// ---- Admin ----

export function useAdminUsers() {
  return useQuery({
    queryKey: ["admin", "users"],
    queryFn: async () => (await api.get<AppUser[]>("/api/admin/users")).data,
  });
}
