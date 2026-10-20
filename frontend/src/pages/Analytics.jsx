import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

function Analytics() {
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
  const [error, setError] = useState("");

  const token = localStorage.getItem("token");

  const loadAnalytics = async (selectedRange) => {
    if (!token) {
      navigate("/login");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const response = await fetch(
        `http://localhost:8080/api/analytics/security?range=${selectedRange}`,
        {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem("token");
        localStorage.removeItem("email");
        navigate("/login");
        return;
      }

      if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "Unable to load security analytics");
      }

      const data = await response.json();

      setAnalytics({
        successfulLogins: data.successfulLogins || 0,
        failedLogins: data.failedLogins || 0,
        suspiciousActivities: data.suspiciousActivities || 0,
        activeSecurityAlerts: data.activeSecurityAlerts || 0,
        recentActivity: Array.isArray(data.recentActivity)
          ? data.recentActivity
          : [],
      });
    } catch (err) {
      console.error(err);
      setError("Unable to load security analytics.");
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
    loadAnalytics(range);
  }, []);

  const handleRangeChange = (e) => {
    const value = e.target.value;
    setRange(value);
    loadAnalytics(value);
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("email");
    navigate("/login");
  };

  const formatDate = (timestamp) => {
    if (!timestamp) return "-";

    const date = new Date(timestamp);

    if (Number.isNaN(date.getTime())) {
      return timestamp;
    }

    return date.toLocaleString();
  };

  const getActivityIcon = (activity) => {
    if (activity.loginStatus === "LOGIN_SUCCESS") {
      return "✅";
    }

    if (activity.loginStatus === "LOGIN_FAILED") {
      return "❌";
    }

    if (activity.action === "SUSPICIOUS_ACTIVITY") {
      return "⚠️";
    }

    if (activity.action === "SECURITY_ALERT") {
      return "🚨";
    }

    return "🔐";
  };

  return (
    <div className="analytics-page">
      <header className="analytics-header">
        <div>
          <p className="analytics-subtitle">SECURITY CENTER</p>
          <h1>Security Analytics</h1>
          <p className="analytics-description">
            Monitor your account security and recent activity.
          </p>
        </div>

        <div className="analytics-header-actions">
          <select
            value={range}
            onChange={handleRangeChange}
            className="range-select"
          >
            <option value="today">Today</option>
            <option value="7">Last 7 Days</option>
            <option value="30">Last 30 Days</option>
          </select>

          <button
            className="analytics-back-btn"
            onClick={() => navigate("/vault")}
          >
            ← Vault
          </button>

          <button
            className="analytics-logout-btn"
            onClick={logout}
          >
            Logout
          </button>
        </div>
      </header>

      {error && (
        <div className="analytics-error">
          {error}
          <button onClick={() => loadAnalytics(range)}>
            Retry
          </button>
        </div>
      )}

      {loading ? (
        <div className="analytics-loading">
          Loading security analytics...
        </div>
      ) : (
        <>
          <section className="analytics-stats">
            <div className="analytics-card success-card">
              <div className="analytics-card-icon">✅</div>
              <div>
                <span>Successful Logins</span>
                <strong>{analytics.successfulLogins}</strong>
              </div>
            </div>

            <div className="analytics-card failed-card">
              <div className="analytics-card-icon">❌</div>
              <div>
                <span>Failed Logins</span>
                <strong>{analytics.failedLogins}</strong>
              </div>
            </div>

            <div className="analytics-card suspicious-card">
              <div className="analytics-card-icon">⚠️</div>
              <div>
                <span>Suspicious Activities</span>
                <strong>{analytics.suspiciousActivities}</strong>
              </div>
            </div>

            <div className="analytics-card alert-card">
              <div className="analytics-card-icon">🚨</div>
              <div>
                <span>Security Alerts</span>
                <strong>{analytics.activeSecurityAlerts}</strong>
              </div>
            </div>
          </section>

          <section className="recent-activity-section">
            <div className="recent-activity-header">
              <div>
                <span className="analytics-section-label">
                  AUDIT MONITORING
                </span>
                <h2>Recent Security Activity</h2>
              </div>

              <span className="activity-count">
                {analytics.recentActivity.length} events
              </span>
            </div>

            {analytics.recentActivity.length === 0 ? (
              <div className="analytics-empty">
                <div className="analytics-empty-icon">🛡️</div>
                <h3>No recent security activity</h3>
                <p>
                  There are no security events for the selected time period.
                </p>
              </div>
            ) : (
              <div className="activity-list">
                {analytics.recentActivity.map((activity) => (
                  <div
                    className="activity-item"
                    key={activity.id}
                  >
                    <div className="activity-icon">
                      {getActivityIcon(activity)}
                    </div>

                    <div className="activity-info">
                      <h3>
                        {activity.action || "Security Activity"}
                      </h3>

                      <p>
                        {activity.details || "Security event recorded"}
                      </p>

                      {activity.loginStatus && (
                        <span className="login-status">
                          {activity.loginStatus}
                        </span>
                      )}
                    </div>

                    <div className="activity-time">
                      {formatDate(activity.timestamp)}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        </>
      )}
    </div>
  );
}

export default Analytics;