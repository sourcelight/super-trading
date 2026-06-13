import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Table, Td, Th } from "@/components/ui/table";
import {
  useCreateSchedule,
  useCredentials,
  useDeleteSchedule,
  useSchedules,
  useUpdateSchedule,
} from "@/hooks/useApi";

export function SchedulesPage() {
  const { data: schedules, isLoading } = useSchedules();
  const { data: credentials } = useCredentials();
  const createSchedule = useCreateSchedule();
  const updateSchedule = useUpdateSchedule();
  const deleteSchedule = useDeleteSchedule();

  const [form, setForm] = useState({ credentialId: "", intervalSeconds: "60", waitBeforeLogoutMs: "3000" });
  const [error, setError] = useState<string | null>(null);

  function submit() {
    setError(null);
    if (!form.credentialId) {
      setError("Pick a credential");
      return;
    }
    createSchedule.mutate(
      {
        credentialId: Number(form.credentialId),
        intervalSeconds: Number(form.intervalSeconds),
        waitBeforeLogoutMs: Number(form.waitBeforeLogoutMs),
      },
      {
        onSuccess: () => setForm({ credentialId: "", intervalSeconds: "60", waitBeforeLogoutMs: "3000" }),
        onError: (e) => setError(e instanceof Error ? e.message : "Failed to create schedule"),
      },
    );
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
      <Card>
        <CardHeader>
          <CardTitle>Schedules</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <p className="text-muted-foreground">Loading…</p>
          ) : (
            <Table>
              <thead>
                <tr>
                  <Th>ID</Th>
                  <Th>Credential</Th>
                  <Th>Every</Th>
                  <Th>Strategy</Th>
                  <Th>State</Th>
                  <Th></Th>
                </tr>
              </thead>
              <tbody>
                {schedules?.map((s) => (
                  <tr key={s.id}>
                    <Td>{s.id}</Td>
                    <Td>#{s.credentialId}</Td>
                    <Td>{s.intervalSeconds}s</Td>
                    <Td>{s.actionStrategy}</Td>
                    <Td>
                      <Badge tone={s.enabled ? "green" : "neutral"}>{s.enabled ? "enabled" : "disabled"}</Badge>
                    </Td>
                    <Td className="flex gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => updateSchedule.mutate({ id: s.id, body: { enabled: !s.enabled } })}
                      >
                        {s.enabled ? "Disable" : "Enable"}
                      </Button>
                      <Button size="sm" variant="destructive" onClick={() => deleteSchedule.mutate(s.id)}>
                        Delete
                      </Button>
                    </Td>
                  </tr>
                ))}
                {schedules?.length === 0 && (
                  <tr>
                    <Td colSpan={6} className="text-muted-foreground">
                      No schedules yet.
                    </Td>
                  </tr>
                )}
              </tbody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Add schedule</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <select
            className="h-9 w-full rounded-md border border-border bg-background px-3 text-sm"
            value={form.credentialId}
            onChange={(e) => setForm({ ...form, credentialId: e.target.value })}
          >
            <option value="">Select a credential…</option>
            {credentials?.map((c) => (
              <option key={c.id} value={c.id}>
                {c.label ?? c.username} ({c.siteName})
              </option>
            ))}
          </select>
          <label className="block text-sm text-muted-foreground">
            Interval (seconds, min 60)
            <Input
              type="number"
              min={60}
              value={form.intervalSeconds}
              onChange={(e) => setForm({ ...form, intervalSeconds: e.target.value })}
            />
          </label>
          <label className="block text-sm text-muted-foreground">
            Wait before logout (ms)
            <Input
              type="number"
              min={0}
              value={form.waitBeforeLogoutMs}
              onChange={(e) => setForm({ ...form, waitBeforeLogoutMs: e.target.value })}
            />
          </label>
          {error && <p className="text-sm text-red-700">{error}</p>}
          <Button onClick={submit} disabled={createSchedule.isPending} className="w-full">
            {createSchedule.isPending ? "Saving…" : "Create schedule"}
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
