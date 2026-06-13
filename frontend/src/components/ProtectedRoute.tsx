import { type ReactNode, useEffect } from "react";
import { useAuth } from "react-oidc-context";
import { setAccessToken } from "@/lib/auth";

// Constant for the app's lifetime, so branching on it keeps hook order stable.
const AUTH_DISABLED = import.meta.env.VITE_AUTH_DISABLED === "true";

/**
 * Gates the app on an authenticated Cognito session. Triggers the PKCE redirect
 * when signed out, and mirrors the access token into the axios/STOMP token holder.
 *
 * <p>Under the local simulation (VITE_AUTH_DISABLED=true) the gate is bypassed entirely:
 * the backend's local profile permits all requests, so no token is needed.
 */
export function ProtectedRoute({ children }: { children: ReactNode }) {
  if (AUTH_DISABLED) {
    return <>{children}</>;
  }
  const auth = useAuth();

  useEffect(() => {
    setAccessToken(auth.user?.access_token ?? null);
  }, [auth.user?.access_token]);

  useEffect(() => {
    if (!auth.isLoading && !auth.isAuthenticated && !auth.activeNavigator && !auth.error) {
      void auth.signinRedirect();
    }
  }, [auth.isLoading, auth.isAuthenticated, auth.activeNavigator, auth.error, auth]);

  if (auth.error) {
    return <div className="p-8 text-red-700">Authentication error: {auth.error.message}</div>;
  }
  if (auth.isLoading || !auth.isAuthenticated) {
    return <div className="p-8 text-muted-foreground">Signing in…</div>;
  }
  return <>{children}</>;
}
