import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

function SecurityAnalytics() {
  const navigate = useNavigate();

  const [analytics, setAnalytics] = useState({
    successfulLogins: 0,
    failedLogins: 0,
    suspiciousActivities: 0,
    activeSecurityAlerts: 0,
    recentActivity: [],
  });

  const [range, setRange] = useState("7");
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");

  const loadAnalytics = async () => {
    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/login");
      return;
    }

    try {
      setLoading(true);
      setMessage("");

      const response = await fetch(
        `http://localhost:8080/api/analytics/security?range=${range}`,
        {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem("token");
        localStorage.removeItem("email");
        navigate("/login");
        return;
      }

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          typeof data === "string"
            ? data
            : "Unable to load security analytics"
        );
      }

      setAnalytics({
        successfulLogins: data.successfulLogins ?? 0,
        failedLogins: data.failedLogins ?? 0,
        suspiciousActivities: data.suspiciousActivities ?? 0,
        activeSecurityAlerts: data.activeSecurityAlerts ?? 0,
        recentActivity: Array.isArray(data.recentActivity)
          ? data.recentActivity
          : [],
      });
    } catch (error) {
      console.error("Analytics error:", error);

      setMessage(
        error.message || "Unable to load security analytics"
      );

      setAnalytics({
        successfulLogins: 0,
        failedLogins: 0,
        suspiciousActivities: 0,
        activeSecurityAlerts: 0,
        recentActivity: [],
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAnalytics();
  }, [range]);

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("email");
    navigate("/login");
  };

  const getActivityIcon = (activity) => {
    const action = (activity.action || "").toUpperCase();
    const status = (activity.loginStatus || "").toUpperCase();

    if (status === "LOGIN_FAILED") {
      return "❌";
    }

    if (status === "LOGIN_SUCCESS") {
      return "✅";
    }

    if (action.includes("SUSPICIOUS")) {
      return "⚠️";
    }

    if (action.includes("SECURITY_ALERT")) {
      return "🚨";
    }

    return "🔐";
  };

  const getActivityStatus = (activity) => {
    const status = (activity.loginStatus || "").toUpperCase();

    if (status === "LOGIN_SUCCESS") {
      return (
        <span className="status-success">
          Successful
        </span>
      );
    }

    if (status === "LOGIN_FAILED") {
      return (
        <span className="status-danger">
          Failed
        </span>
      );
    }

    if (
      (activity.action || "")
        .toUpperCase()
        .includes("SECURITY_ALERT")
    ) {
      return (
        <span className="status-danger">
          Alert
        </span>
      );
    }

    if (
      (activity.action || "")
        .toUpperCase()
        .includes("SUSPICIOUS")
    ) {
      return (
        <span className="status-warning">
          Suspicious
        </span>
      );
    }

    return (
      <span className="status-warning">
        Activity
      </span>
    );
  };

  return (
    <div className="vault-page">

      <aside className="sidebar">

        <div className="brand">
          <div className="brand-icon">
            🔐
          </div>

          <div>
            <h2>SecureVault</h2>
            <span>Password Manager</span>
          </div>
        </div>

        <nav>

          <div
            className="nav-item"
            onClick={() => navigate("/vault")}
          >
            <span>🔑</span>
            Vault
          </div>

          <div
            className="nav-item"
            onClick={() => navigate("/shared-vault")}
          >
            <span>🔗</span>
            Shared Vault
          </div>

          <div
            className="nav-item"
            onClick={() => navigate("/team-vault")}
          >
            <span>👥</span>
            Team Vault
          </div>

          <div
            className="nav-item active"
            onClick={() => navigate("/security-analytics")}
          >
            <span>📊</span>
            Security Analytics
          </div>

          <div
            className="nav-item"
            onClick={() => navigate("/settings")}
          >
            <span>⚙️</span>
            Settings
          </div>

        </nav>

        <div className="security-box">
          <div>🛡️</div>
          <strong>Secure & Protected</strong>
          <p>AES-256 encrypted storage</p>
        </div>

      </aside>

      <main className="main-content">

        <header className="topbar">

          <div>
            <p className="welcome-small">
              Security Monitoring Center
            </p>

            <h1>
              Security Analytics
            </h1>
          </div>

          <button
            className="logout-btn"
            onClick={logout}
          >
            Logout
          </button>

        </header>

        <section className="credential-form analytics-header">

          <div>
            <span className="section-label">
              SECURITY ANALYTICS
            </span>

            <h2>
              Account Security Overview
            </h2>

            <p>
              Monitor login activity, suspicious activity,
              alerts and recent security events.
            </p>
          </div>

          <select
            value={range}
            onChange={(e) =>
              setRange(e.target.value)
            }
            className="analytics-select"
          >
            <option value="today">
              Today
            </option>

            <option value="7">
              Last 7 Days
            </option>

            <option value="30">
              Last 30 Days
            </option>
          </select>

        </section>

        {message && (
          <div className="message analytics-error">
            {message}
          </div>
        )}

        {loading ? (

          <div className="empty-state">

            <div className="empty-icon">
              ⏳
            </div>

            <h3>
              Loading Security Analytics...
            </h3>

            <p>
              Please wait.
            </p>

          </div>

        ) : (

          <>

            <section className="stats">

              <div className="stat-card analytics-card success">

                <span className="stat-icon">
                  ✅
                </span>

                <div>
                  <p>
                    Successful Logins
                  </p>

                  <h2>
                    {analytics.successfulLogins}
                  </h2>
                </div>

              </div>

              <div className="stat-card analytics-card danger">

                <span className="stat-icon">
                  ❌
                </span>

                <div>
                  <p>
                    Failed Logins
                  </p>

                  <h2>
                    {analytics.failedLogins}
                  </h2>
                </div>

              </div>

              <div className="stat-card analytics-card warning">

                <span className="stat-icon">
                  ⚠️
                </span>

                <div>
                  <p>
                    Suspicious Activities
                  </p>

                  <h2>
                    {analytics.suspiciousActivities}
                  </h2>
                </div>

              </div>

              <div className="stat-card analytics-card alert">

                <span className="stat-icon">
                  🚨
                </span>

                <div>
                  <p>
                    Active Security Alerts
                  </p>

                  <h2>
                    {analytics.activeSecurityAlerts}
                  </h2>
                </div>

              </div>

            </section>

            <section className="saved-section">

              <div className="saved-header">

                <div>
                  <span className="section-label">
                    SECURITY LOG
                  </span>

                  <h2>
                    Recent Security Activity
                  </h2>
                </div>

                <button
                  className="generate-btn"
                  onClick={loadAnalytics}
                >
                  ↻ Refresh
                </button>

              </div>

              {analytics.recentActivity.length === 0 ? (

                <div className="empty-state">

                  <div className="empty-icon">
                    🛡️
                  </div>

                  <h3>
                    No Recent Security Activity
                  </h3>

                  <p>
                    There are no security events for
                    the selected time period.
                  </p>

                </div>

              ) : (

                <div className="credential-list">

                  {analytics.recentActivity.map(
                    (activity, index) => (

                      <div
                        className="credential-card activity-card"
                        key={
                          activity.id || index
                        }
                      >

                        <div className="credential-logo">
                          {getActivityIcon(activity)}
                        </div>

                        <div className="credential-info">

                          <h3>
                            {activity.action ||
                              "Security Event"}
                          </h3>

                          <p>
                            {activity.details ||
                              "Security activity detected"}
                          </p>

                          {activity.ipAddress && (
                            <p>
                              IP: {activity.ipAddress}
                            </p>
                          )}

                          {activity.timestamp && (
                            <p>
                              {new Date(
                                activity.timestamp
                              ).toLocaleString()}
                            </p>
                          )}

                        </div>

                        <div className="activity-status">
                          {getActivityStatus(activity)}
                        </div>

                      </div>

                    )
                  )}

                </div>

              )}

            </section>

          </>

        )}

      </main>

    </div>
  );
}

export default SecurityAnalytics;