import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Table, Td, Th } from "@/components/ui/table";
import { useCreateCredential, useCredentials, useSites } from "@/hooks/useApi";
import { formatDateTime } from "@/lib/utils";

export function CredentialsPage() {
  const { data: credentials, isLoading } = useCredentials();
  const { data: sites } = useSites();
  const createCredential = useCreateCredential();
  const [form, setForm] = useState({ siteId: "", label: "", username: "", password: "" });
  const [error, setError] = useState<string | null>(null);

  function submit() {
    setError(null);
    if (!form.siteId) {
      setError("Pick a site");
      return;
    }
    createCredential.mutate(
      {
        siteId: Number(form.siteId),
        label: form.label || undefined,
        username: form.username,
        password: form.password,
      },
      {
        onSuccess: () => setForm({ siteId: "", label: "", username: "", password: "" }),
        onError: (e) => setError(e instanceof Error ? e.message : "Failed to create credential"),
      },
    );
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
      <Card>
        <CardHeader>
          <CardTitle>Credentials</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <p className="text-muted-foreground">Loading…</p>
          ) : (
            <Table>
              <thead>
                <tr>
                  <Th>Label</Th>
                  <Th>Site</Th>
                  <Th>Username</Th>
                  <Th>Created</Th>
                </tr>
              </thead>
              <tbody>
                {credentials?.map((c) => (
                  <tr key={c.id}>
                    <Td>{c.label ?? "—"}</Td>
                    <Td>{c.siteName}</Td>
                    <Td>{c.username}</Td>
                    <Td>{formatDateTime(c.createdAt)}</Td>
                  </tr>
                ))}
                {credentials?.length === 0 && (
                  <tr>
                    <Td colSpan={4} className="text-muted-foreground">
                      No credentials yet.
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
          <CardTitle>Add credential</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <select
            className="h-9 w-full rounded-md border border-border bg-background px-3 text-sm"
            value={form.siteId}
            onChange={(e) => setForm({ ...form, siteId: e.target.value })}
          >
            <option value="">Select a site…</option>
            {sites?.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
          <Input placeholder="Label (optional)" value={form.label} onChange={(e) => setForm({ ...form, label: e.target.value })} />
          <Input placeholder="Username" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} />
          <Input
            type="password"
            placeholder="Password"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
          />
          <p className="text-xs text-muted-foreground">
            The password is sent once and stored in Secrets Manager — never returned by the API.
          </p>
          {error && <p className="text-sm text-red-700">{error}</p>}
          <Button onClick={submit} disabled={createCredential.isPending} className="w-full">
            {createCredential.isPending ? "Saving…" : "Create credential"}
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
