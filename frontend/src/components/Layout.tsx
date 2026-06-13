import { useAuth } from "react-oidc-context";
import { NavLink, Outlet } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

const NAV = [
  { to: "/sites", label: "Sites" },
  { to: "/credentials", label: "Credentials" },
  { to: "/schedules", label: "Schedules" },
  { to: "/monitoring", label: "Monitoring" },
  { to: "/history", label: "History" },
];

function isAdmin(profile: Record<string, unknown> | undefined): boolean {
  const groups = profile?.["cognito:groups"];
  return Array.isArray(groups) && groups.includes("ADMIN");
}

export function Layout() {
  const auth = useAuth();
  const admin = isAdmin(auth.user?.profile as Record<string, unknown> | undefined);

  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b border-border">
        <div className="mx-auto max-w-6xl px-4 h-14 flex items-center gap-6">
          <span className="font-semibold">super-trading</span>
          <nav className="flex items-center gap-1 flex-1">
            {[...NAV, ...(admin ? [{ to: "/admin", label: "Admin" }] : [])].map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  cn(
                    "px-3 py-1.5 rounded-md text-sm hover:bg-muted",
                    isActive && "bg-muted font-medium",
                  )
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
          <span className="text-sm text-muted-foreground">
            {auth.user?.profile.email ?? auth.user?.profile.sub}
          </span>
          <Button variant="outline" size="sm" onClick={() => void auth.removeUser()}>
            Sign out
          </Button>
        </div>
      </header>
      <main className="mx-auto w-full max-w-6xl px-4 py-6 flex-1">
        <Outlet />
      </main>
    </div>
  );
}
