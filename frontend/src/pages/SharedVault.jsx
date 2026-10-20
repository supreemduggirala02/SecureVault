import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import "../App.css";

function SharedVault() {
  const navigate = useNavigate();

  const [sharedCredentials, setSharedCredentials] = useState([]);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);

  const token = localStorage.getItem("token");

  const authHeaders = {
    Authorization: `Bearer ${token}`,
  };

  const loadSharedCredentials = async () => {
    try {
      setLoading(true);

      const response = await fetch(
        "http://localhost:8080/api/sharing/shared-with-me",
        {
          method: "GET",
          headers: authHeaders,
        }
      );

      if (response.ok) {
        const data = await response.json();
        setSharedCredentials(Array.isArray(data) ? data : []);
      } else {
        setMessage("Unable to load shared credentials");
      }
    } catch (error) {
      setMessage("Backend connection failed");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!token) {
      navigate("/login");
      return;
    }

    loadSharedCredentials();
  }, []);

  const openCredential = (share) => {
    const permission = share?.permission;

    if (permission === "VIEW_ONLY") {
      setMessage("This credential is View Only");
      return;
    }

    const credentialId =
      share?.credential?.id || share?.credentialId;

    if (!credentialId) {
      setMessage("Credential ID not found");
      return;
    }

    navigate(`/vault?edit=${credentialId}`);
  };

  const logout = () => {
    localStorage.removeItem("token");
    navigate("/login");
  };

  return (
    <div className="vault-page">

      {/* SIDEBAR */}

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
            className="nav-item active"
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
            className="nav-item"
            onClick={() => navigate("/settings")}
          >
            <span>⚙️</span>
            Settings
          </div>

        </nav>

        <div className="security-box">

          <div>🛡️</div>

          <strong>
            Secure & Protected
          </strong>

          <p>
            AES-256 encrypted storage
          </p>

        </div>

      </aside>

      {/* MAIN CONTENT */}

      <main className="main-content">

        {/* TOPBAR */}

        <header className="topbar">

          <div>

            <p className="welcome-small">
              Collaboration Center
            </p>

            <h1>
              Shared Vault
            </h1>

          </div>

          <button
            className="logout-btn"
            onClick={logout}
          >
            Logout
          </button>

        </header>

        {/* INTRO CARD */}

        <section className="credential-form">

          <div className="section-heading">

            <div>

              <span className="section-label">
                SECURE SHARING
              </span>

              <h2>
                Credentials shared with you
              </h2>

              <p>
                Access credentials securely shared by
                other SecureVault users.
              </p>

            </div>

          </div>

        </section>

        {/* STATS */}

        <section className="stats">

          <div className="stat-card">

            <span className="stat-icon">
              🔗
            </span>

            <div>

              <p>
                Shared Credentials
              </p>

              <h2>
                {sharedCredentials.length}
              </h2>

            </div>

          </div>

          <div className="stat-card">

            <span className="stat-icon">
              👁️
            </span>

            <div>

              <p>
                View Only
              </p>

              <h2>
                {
                  sharedCredentials.filter(
                    (item) =>
                      item.permission === "VIEW_ONLY"
                  ).length
                }
              </h2>

            </div>

          </div>

          <div className="stat-card">

            <span className="stat-icon">
              ✏️
            </span>

            <div>

              <p>
                Editable
              </p>

              <h2>
                {
                  sharedCredentials.filter(
                    (item) =>
                      item.permission === "EDIT" ||
                      item.permission ===
                        "FULL_MANAGEMENT"
                  ).length
                }
              </h2>

            </div>

          </div>

        </section>

        {/* MESSAGE */}

        {message && (
          <div className="message">
            {message}
          </div>
        )}

        {/* SHARED CREDENTIALS */}

        <section className="saved-section">

          <div className="saved-header">

            <div>

              <span className="section-label">
                SHARED WITH ME
              </span>

              <h2>
                Available Credentials
              </h2>

            </div>

            <button
              className="generate-btn"
              onClick={loadSharedCredentials}
            >
              ↻ Refresh
            </button>

          </div>

          {loading ? (

            <div className="empty-state">

              <div className="empty-icon">
                ⏳
              </div>

              <h3>
                Loading credentials...
              </h3>

              <p>
                Please wait while we securely load
                your shared credentials.
              </p>

            </div>

          ) : sharedCredentials.length === 0 ? (

            <div className="empty-state">

              <div className="empty-icon">
                🔗
              </div>

              <h3>
                No shared credentials
              </h3>

              <p>
                When another user shares a credential
                with you, it will appear here.
              </p>

              <button
                className="primary-btn"
                onClick={() => navigate("/vault")}
              >
                Go to My Vault
              </button>

            </div>

          ) : (

            <div className="credential-list">

              {sharedCredentials.map((share) => {

                const credential =
                  share?.credential;

                if (!credential) {
                  return null;
                }

                const permission =
                  share?.permission || "VIEW_ONLY";

                const isViewOnly =
                  permission === "VIEW_ONLY";

                const isEditable =
                  permission === "EDIT";

                const isFullManagement =
                  permission ===
                  "FULL_MANAGEMENT";

                return (

                  <div
                    className="credential-card"
                    key={share.id}
                  >

                    {/* ICON */}

                    <div className="credential-logo">
                      🔗
                    </div>

                    {/* DETAILS */}

                    <div className="credential-info">

                      <h3>
                        {credential.title}
                      </h3>

                      <p>
                        {credential.username}
                      </p>

                      <p>

                        Permission:{" "}

                        <strong>

                          {permission ===
                            "VIEW_ONLY" &&
                            "View Only"}

                          {permission ===
                            "EDIT" &&
                            "Edit"}

                          {permission ===
                            "FULL_MANAGEMENT" &&
                            "Full Management"}

                        </strong>

                      </p>

                      {share.expiresAt && (

                        <p>

                          Expires:{" "}

                          <strong>
                            {share.expiresAt}
                          </strong>

                        </p>

                      )}

                      <p>

                        Access:{" "}

                        <strong>
                          {isViewOnly
                            ? "Read Only"
                            : "Editable"}
                        </strong>

                      </p>

                    </div>

                    {/* ACTIONS */}

                    <div className="form-actions">

                      {isViewOnly && (

                        <span
                          className="security-badge"
                        >
                          👁️ View Only
                        </span>

                      )}

                      {isEditable && (

                        <button
                          className="primary-btn"
                          type="button"
                          onClick={() =>
                            openCredential(share)
                          }
                        >
                          ✏️ Edit
                        </button>

                      )}

                      {isFullManagement && (

                        <button
                          className="primary-btn"
                          type="button"
                          onClick={() =>
                            openCredential(share)
                          }
                        >
                          🔐 Manage
                        </button>

                      )}

                    </div>

                  </div>

                );
              })}

            </div>

          )}

        </section>

        {/* SECURITY INFORMATION */}

        <section className="credential-form">

          <div className="section-heading">

            <div>

              <span className="section-label">
                SECURITY
              </span>

              <h2>
                How shared access works
              </h2>

            </div>

          </div>

          <div className="form-grid">

            <div className="stat-card">

              <span className="stat-icon">
                🔐
              </span>

              <div>

                <h3>
                  Protected Access
                </h3>

                <p>
                  Shared credentials are protected
                  by your SecureVault security layer.
                </p>

              </div>

            </div>

            <div className="stat-card">

              <span className="stat-icon">
                ⏱️
              </span>

              <div>

                <h3>
                  Expiring Access
                </h3>

                <p>
                  Credential owners can configure
                  an expiry time for shared access.
                </p>

              </div>

            </div>

            <div className="stat-card">

              <span className="stat-icon">
                🛡️
              </span>

              <div>

                <h3>
                  Permission Control
                </h3>

                <p>
                  View Only, Edit and Full Management
                  permissions are supported.
                </p>

              </div>

            </div>

          </div>

        </section>

      </main>

    </div>
  );
}

export default SharedVault;