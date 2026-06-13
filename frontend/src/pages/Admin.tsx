import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, Td, Th } from "@/components/ui/table";
import { useAdminUsers } from "@/hooks/useApi";

export function AdminPage() {
  const { data: users, isLoading, error } = useAdminUsers();

  return (
    <Card>
      <CardHeader>
        <CardTitle>Users</CardTitle>
      </CardHeader>
      <CardContent>
        {error ? (
          <p className="text-red-700">You do not have access to this area.</p>
        ) : isLoading ? (
          <p className="text-muted-foreground">Loading…</p>
        ) : (
          <Table>
            <thead>
              <tr>
                <Th>ID</Th>
                <Th>Email</Th>
                <Th>Name</Th>
                <Th>Role</Th>
                <Th>Enabled</Th>
              </tr>
            </thead>
            <tbody>
              {users?.map((u) => (
                <tr key={u.id}>
                  <Td>{u.id}</Td>
                  <Td>{u.email}</Td>
                  <Td>{u.displayName ?? "—"}</Td>
                  <Td>
                    <Badge tone={u.role === "ADMIN" ? "blue" : "neutral"}>{u.role}</Badge>
                  </Td>
                  <Td>{u.enabled ? "yes" : "no"}</Td>
                </tr>
              ))}
            </tbody>
          </Table>
        )}
      </CardContent>
    </Card>
  );
}
