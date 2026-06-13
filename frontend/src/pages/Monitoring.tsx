import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, Td, Th } from "@/components/ui/table";
import { StatusBadge } from "@/components/StatusBadge";
import { useMonitoring } from "@/ws/useMonitoring";
import { formatDateTime } from "@/lib/utils";

export function MonitoringPage() {
  const { executions, connected } = useMonitoring();

  return (
    <Card>
      <CardHeader className="flex-row items-center justify-between">
        <CardTitle>Live monitoring</CardTitle>
        <Badge tone={connected ? "green" : "amber"}>{connected ? "connected" : "connecting…"}</Badge>
      </CardHeader>
      <CardContent>
        <Table>
          <thead>
            <tr>
              <Th>Execution</Th>
              <Th>Schedule</Th>
              <Th>Status</Th>
              <Th>Started</Th>
              <Th>Ended</Th>
              <Th>Error</Th>
            </tr>
          </thead>
          <tbody>
            {executions.map((e) => (
              <tr key={e.id}>
                <Td>#{e.id}</Td>
                <Td>#{e.scheduleId}</Td>
                <Td>
                  <StatusBadge status={e.status} />
                </Td>
                <Td>{formatDateTime(e.startedAt)}</Td>
                <Td>{formatDateTime(e.endedAt)}</Td>
                <Td className="text-red-700">{e.errorMessage ?? ""}</Td>
              </tr>
            ))}
            {executions.length === 0 && (
              <tr>
                <Td colSpan={6} className="text-muted-foreground">
                  Waiting for run activity…
                </Td>
              </tr>
            )}
          </tbody>
        </Table>
      </CardContent>
    </Card>
  );
}
