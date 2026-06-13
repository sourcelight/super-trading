import { useState } from "react";
import { Bar, BarChart, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, Td, Th } from "@/components/ui/table";
import { StatusBadge } from "@/components/StatusBadge";
import { useChoiceStats, useExecutions, useSchedules } from "@/hooks/useApi";
import { formatDateTime } from "@/lib/utils";

export function HistoryPage() {
  const { data: schedules } = useSchedules();
  const [scheduleId, setScheduleId] = useState<number | null>(null);
  const { data: executions } = useExecutions(scheduleId);
  const { data: stats } = useChoiceStats();

  const chartData = stats
    ? [
        { name: "GREEN", value: stats.green, fill: "#16a34a" },
        { name: "RED", value: stats.red, fill: "#dc2626" },
      ]
    : [];

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>GREEN vs RED (last 7 days)</CardTitle>
        </CardHeader>
        <CardContent style={{ height: 280 }}>
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis allowDecimals={false} />
              <Tooltip />
              <Bar dataKey="value">
                {chartData.map((entry) => (
                  <Cell key={entry.name} fill={entry.fill} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex-row items-center justify-between">
          <CardTitle>Execution history</CardTitle>
          <select
            className="h-9 rounded-md border border-border bg-background px-3 text-sm"
            value={scheduleId ?? ""}
            onChange={(e) => setScheduleId(e.target.value ? Number(e.target.value) : null)}
          >
            <option value="">Select a schedule…</option>
            {schedules?.map((s) => (
              <option key={s.id} value={s.id}>
                Schedule #{s.id} (every {s.intervalSeconds}s)
              </option>
            ))}
          </select>
        </CardHeader>
        <CardContent>
          {scheduleId == null ? (
            <p className="text-muted-foreground">Pick a schedule to see its runs.</p>
          ) : (
            <Table>
              <thead>
                <tr>
                  <Th>Execution</Th>
                  <Th>Status</Th>
                  <Th>Started</Th>
                  <Th>Ended</Th>
                </tr>
              </thead>
              <tbody>
                {executions?.map((e) => (
                  <tr key={e.id}>
                    <Td>#{e.id}</Td>
                    <Td>
                      <StatusBadge status={e.status} />
                    </Td>
                    <Td>{formatDateTime(e.startedAt)}</Td>
                    <Td>{formatDateTime(e.endedAt)}</Td>
                  </tr>
                ))}
                {executions?.length === 0 && (
                  <tr>
                    <Td colSpan={4} className="text-muted-foreground">
                      No runs in the selected window.
                    </Td>
                  </tr>
                )}
              </tbody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
