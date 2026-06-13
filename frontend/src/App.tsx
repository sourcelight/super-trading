import { Navigate, Route, Routes } from "react-router-dom";
import { Layout } from "@/components/Layout";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { AdminPage } from "@/pages/Admin";
import { CredentialsPage } from "@/pages/Credentials";
import { HistoryPage } from "@/pages/History";
import { MonitoringPage } from "@/pages/Monitoring";
import { SchedulesPage } from "@/pages/Schedules";
import { SitesPage } from "@/pages/Sites";

export function App() {
  return (
    <ProtectedRoute>
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<Navigate to="/sites" replace />} />
          <Route path="/sites" element={<SitesPage />} />
          <Route path="/credentials" element={<CredentialsPage />} />
          <Route path="/schedules" element={<SchedulesPage />} />
          <Route path="/monitoring" element={<MonitoringPage />} />
          <Route path="/history" element={<HistoryPage />} />
          <Route path="/admin" element={<AdminPage />} />
          <Route path="*" element={<Navigate to="/sites" replace />} />
        </Route>
      </Routes>
    </ProtectedRoute>
  );
}
