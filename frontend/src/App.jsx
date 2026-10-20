
import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";

import Login from "./pages/Login";
import Register from "./pages/Register";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";

import Vault from "./pages/Vault";
import SharedVault from "./pages/SharedVault";
import TeamVault from "./pages/TeamVault";
import Settings from "./pages/Settings";
import Reports from "./pages/Reports";
import SecurityAnalytics from "./pages/SecurityAnalytics";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Authentication routes */}
        <Route
          path="/"
          element={<Navigate to="/login" replace />}
        />

        <Route
          path="/login"
          element={<Login />}
        />

        <Route
          path="/register"
          element={<Register />}
        />

        <Route
          path="/forgot-password"
          element={<ForgotPassword />}
        />

        <Route
          path="/reset-password"
          element={<ResetPassword />}
        />

        {/* Application routes */}
        <Route
          path="/vault"
          element={<Vault />}
        />

        <Route
          path="/shared-vault"
          element={<SharedVault />}
        />

        <Route
          path="/team-vault"
          element={<TeamVault />}
        />

        <Route
          path="/settings"
          element={<Settings />}
        />

        <Route
          path="/reports"
          element={<Reports />}
        />

        {/* Security Analytics */}
        <Route
          path="/security-analytics"
          element={<SecurityAnalytics />}
        />

        {/* Unknown route */}
        <Route
          path="*"
          element={<Navigate to="/login" replace />}
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;