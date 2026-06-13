import { Badge } from "@/components/ui/badge";
import type { ExecutionStatus } from "@/types/api";

export function StatusBadge({ status }: { status: ExecutionStatus }) {
  const tone = status === "SUCCESS" ? "green" : status === "FAILED" ? "red" : "amber";
  return <Badge tone={tone}>{status}</Badge>;
}
