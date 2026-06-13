import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Table, Td, Th } from "@/components/ui/table";
import { useCreateSite, useSites } from "@/hooks/useApi";
import { formatDateTime } from "@/lib/utils";

const DEFAULT_SELECTORS = JSON.stringify(
  { username: "#username", password: "#password", green: ".btn-green", red: ".btn-red", logout: "#logout" },
  null,
  2,
);

export function SitesPage() {
  const { data: sites, isLoading } = useSites();
  const createSite = useCreateSite();
  const [form, setForm] = useState({ name: "", baseUrl: "", loginUrl: "", selectors: DEFAULT_SELECTORS });
  const [error, setError] = useState<string | null>(null);

  function submit() {
    setError(null);
    let selectors: Record<string, string>;
    try {
      selectors = JSON.parse(form.selectors);
    } catch {
      setError("Selectors must be valid JSON");
      return;
    }
    createSite.mutate(
      { name: form.name, baseUrl: form.baseUrl, loginUrl: form.loginUrl, selectors },
      {
        onSuccess: () => setForm({ name: "", baseUrl: "", loginUrl: "", selectors: DEFAULT_SELECTORS }),
        onError: (e) => setError(e instanceof Error ? e.message : "Failed to create site"),
      },
    );
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
      <Card>
        <CardHeader>
          <CardTitle>Sites</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <p className="text-muted-foreground">Loading…</p>
          ) : (
            <Table>
              <thead>
                <tr>
                  <Th>Name</Th>
                  <Th>Base URL</Th>
                  <Th>Created</Th>
                </tr>
              </thead>
              <tbody>
                {sites?.map((s) => (
                  <tr key={s.id}>
                    <Td>{s.name}</Td>
                    <Td className="text-muted-foreground">{s.baseUrl}</Td>
                    <Td>{formatDateTime(s.createdAt)}</Td>
                  </tr>
                ))}
                {sites?.length === 0 && (
                  <tr>
                    <Td colSpan={3} className="text-muted-foreground">
                      No sites yet.
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
          <CardTitle>Add site</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <Input placeholder="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <Input placeholder="Base URL" value={form.baseUrl} onChange={(e) => setForm({ ...form, baseUrl: e.target.value })} />
          <Input placeholder="Login URL" value={form.loginUrl} onChange={(e) => setForm({ ...form, loginUrl: e.target.value })} />
          <textarea
            className="w-full h-44 rounded-md border border-border bg-background p-2 font-mono text-xs outline-none focus:ring-2 focus:ring-primary/30"
            value={form.selectors}
            onChange={(e) => setForm({ ...form, selectors: e.target.value })}
          />
          {error && <p className="text-sm text-red-700">{error}</p>}
          <Button onClick={submit} disabled={createSite.isPending} className="w-full">
            {createSite.isPending ? "Saving…" : "Create site"}
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
