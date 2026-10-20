import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../App.css";
import NotificationBell from "../Components/NotificationBell";

function Vault() {
  const navigate = useNavigate();

  const [title, setTitle] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const [strength, setStrength] = useState("");
  const [message, setMessage] = useState("");

  const [credentials, setCredentials] = useState([]);
  const [editingId, setEditingId] = useState(null);
  const [showPassword, setShowPassword] = useState(false);
  const [search, setSearch] = useState("");

  const [sharedUserEmail, setSharedUserEmail] = useState("");
  const [permission, setPermission] = useState("VIEW_ONLY");
  const [expiresAt, setExpiresAt] = useState("");

  const token = localStorage.getItem("token");

  const authHeaders = {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  };

  // =========================================================
  // LOGOUT
  // =========================================================

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("email");
    navigate("/login");
  };

  // =========================================================
  // AUTH CHECK
  // =========================================================

  const checkAuth = () => {
    if (!token) {
      navigate("/login");
      return false;
    }

    return true;
  };

  // =========================================================
  // LOAD CREDENTIALS
  // =========================================================

  const loadCredentials = async () => {
    if (!checkAuth()) return;

    try {
      const response = await fetch(
        "http://localhost:8080/api/vault/credentials",
        {
          method: "GET",
          headers: authHeaders,
        }
      );

      if (response.status === 401 || response.status === 403) {
        logout();
        return;
      }

      if (!response.ok) {
        const error = await response.text();
        setMessage(error || "Unable to load credentials");
        return;
      }

      const data = await response.json();

      setCredentials(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error(error);
      setMessage("Backend connection failed");
    }
  };

  useEffect(() => {
    loadCredentials();
  }, []);

  // =========================================================
  // PASSWORD STRENGTH
  // =========================================================

  const checkStrength = (value) => {
    setPassword(value);

    if (!value) {
      setStrength("");
      return;
    }

    let score = 0;

    if (value.length >= 8) score++;
    if (/[A-Z]/.test(value)) score++;
    if (/[a-z]/.test(value)) score++;
    if (/[0-9]/.test(value)) score++;
    if (/[^A-Za-z0-9]/.test(value)) score++;

    if (score <= 2) {
      setStrength("Weak");
    } else if (score <= 4) {
      setStrength("Medium");
    } else {
      setStrength("Strong");
    }
  };

  // =========================================================
  // RESET FORM
  // =========================================================

  const resetForm = () => {
    setTitle("");
    setUsername("");
    setPassword("");
    setStrength("");
    setEditingId(null);
    setShowPassword(false);
  };

  // =========================================================
  // SAVE / UPDATE CREDENTIAL
  // =========================================================

  const saveCredential = async (e) => {
    e.preventDefault();

    if (!title.trim() || !username.trim()) {
      setMessage("Please fill title and username");
      return;
    }

    if (!editingId && !password.trim()) {
      setMessage("Password is required for a new credential");
      return;
    }

    try {
      const url = editingId
        ? `http://localhost:8080/api/vault/credentials/${editingId}`
        : "http://localhost:8080/api/vault/credentials";

      const response = await fetch(url, {
        method: editingId ? "PUT" : "POST",
        headers: authHeaders,
        body: JSON.stringify({
          title: title.trim(),
          username: username.trim(),
          password: password,
        }),
      });

      const data = await response.text();

      if (response.status === 401 || response.status === 403) {
        logout();
        return;
      }

      if (!response.ok) {
        setMessage(data || "Unable to save credential");
        return;
      }

      setMessage(
        editingId
          ? "Credential updated successfully"
          : "Credential saved successfully"
      );

      resetForm();
      await loadCredentials();
    } catch (error) {
      console.error(error);
      setMessage("Backend connection failed");
    }
  };

  // =========================================================
  // EDIT CREDENTIAL
  // =========================================================

  const editCredential = (credential) => {
    setEditingId(credential.id);
    setTitle(credential.title || "");
    setUsername(credential.username || "");

    /*
     * SECURITY:
     * Backend does not return the existing password.
     * User enters a new password only when changing it.
     */
    setPassword("");

    setStrength("");
    setShowPassword(false);
    setMessage("");

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  // =========================================================
  // DELETE CREDENTIAL
  // =========================================================

  const deleteCredential = async (id) => {
    const confirmed = window.confirm(
      "Are you sure you want to delete this credential?"
    );

    if (!confirmed) return;

    try {
      const response = await fetch(
        `http://localhost:8080/api/vault/credentials/${id}`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      const data = await response.text();

      if (response.status === 401 || response.status === 403) {
        setMessage(data || "Access denied");
        return;
      }

      if (!response.ok) {
        setMessage(data || "Unable to delete credential");
        return;
      }

      setMessage("Credential deleted successfully");

      await loadCredentials();
    } catch (error) {
      console.error(error);
      setMessage("Backend connection failed");
    }
  };

  // =========================================================
  // SHARE CREDENTIAL
  // =========================================================

  const shareCredential = async (credentialId) => {
    if (!sharedUserEmail.trim()) {
      setMessage("Please enter user email");
      return;
    }

    try {
      const params = new URLSearchParams();

      params.append("credentialId", credentialId);
      params.append(
        "sharedUserEmail",
        sharedUserEmail.trim()
      );
      params.append("permission", permission);

      if (expiresAt) {
        params.append("expiresAt", expiresAt);
      }

      const response = await fetch(
        `http://localhost:8080/api/sharing/share?${params.toString()}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      const data = await response.text();

      if (response.status === 401 || response.status === 403) {
        logout();
        return;
      }

      if (!response.ok) {
        setMessage(data || "Unable to share credential");
        return;
      }

      setMessage("Credential shared successfully");

      setSharedUserEmail("");
      setPermission("VIEW_ONLY");
      setExpiresAt("");
    } catch (error) {
      console.error(error);
      setMessage("Backend connection failed");
    }
  };

  // =========================================================
  // SEARCH
  // =========================================================

  const filteredCredentials = credentials.filter(
    (credential) => {
      const text =
        `${credential.title || ""} ${
          credential.username || ""
        }`.toLowerCase();

      return text.includes(search.toLowerCase());
    }
  );

  // =========================================================
  // UI
  // =========================================================

  return (
    <div className="vault-page">

      {/* =====================================================
          SIDEBAR
      ===================================================== */}

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
            className="nav-item active"
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
            className="nav-item"
            onClick={() =>
              navigate("/security-analytics")
            }
          >
            <span>📊</span>
            Security Analytics
          </div>

          <div
            className="nav-item"
            onClick={() => navigate("/reports")}
          >
            <span>📑</span>
            Reports
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

      {/* =====================================================
          MAIN CONTENT
      ===================================================== */}

      <main className="main-content">

        {/* ===================================================
            TOP BAR
        =================================================== */}

        <header className="topbar">

          <div className="topbar-heading">

            <p className="welcome-small">
              Welcome to your secure space 🔐
            </p>

            <h1>
              My Vault
            </h1>

          </div>

          <div className="topbar-actions">

            {/* Notification Bell */}
            <NotificationBell />

            {/* Logout */}
            <button
              className="logout-btn"
              onClick={logout}
            >
              Logout
            </button>

          </div>

        </header>

        {/* ===================================================
            STATS
        =================================================== */}

        <section className="stats">

          <div className="stat-card">

            <span className="stat-icon">
              🔑
            </span>

            <div>
              <p>
                Saved Credentials
              </p>

              <h2>
                {credentials.length}
              </h2>
            </div>

          </div>

          <div className="stat-card">

            <span className="stat-icon">
              🔗
            </span>

            <div>
              <p>
                Sharing
              </p>

              <h2>
                Secure
              </h2>
            </div>

          </div>

          <div className="stat-card">

            <span className="stat-icon">
              🛡️
            </span>

            <div>
              <p>
                Security
              </p>

              <h2>
                Protected
              </h2>
            </div>

          </div>

        </section>

        {/* ===================================================
            ADD / EDIT FORM
        =================================================== */}

        <section className="credential-form">

          <span className="section-label">
            {editingId
              ? "EDIT CREDENTIAL"
              : "ADD CREDENTIAL"}
          </span>

          <h2>
            {editingId
              ? "Update Credential"
              : "Add New Credential"}
          </h2>

          <form onSubmit={saveCredential}>

            <div className="form-grid">

              {/* TITLE */}

              <div className="input-group">

                <label>
                  Title
                </label>

                <input
                  type="text"
                  placeholder="Gmail"
                  value={title}
                  onChange={(e) =>
                    setTitle(e.target.value)
                  }
                />

              </div>

              {/* USERNAME */}

              <div className="input-group">

                <label>
                  Username / Email
                </label>

                <input
                  type="text"
                  placeholder="you@example.com"
                  value={username}
                  onChange={(e) =>
                    setUsername(e.target.value)
                  }
                />

              </div>

              {/* PASSWORD */}

              <div className="input-group">

                <label>
                  Password
                </label>

                <div className="password-input-wrapper">

                  <input
                    type={
                      showPassword
                        ? "text"
                        : "password"
                    }
                    placeholder={
                      editingId
                        ? "Enter new password only if changing it"
                        : "Enter password"
                    }
                    value={password}
                    onChange={(e) =>
                      checkStrength(
                        e.target.value
                      )
                    }
                  />

                  <button
                    type="button"
                    className="show-password-btn"
                    onClick={() =>
                      setShowPassword(
                        !showPassword
                      )
                    }
                  >
                    {showPassword
                      ? "Hide"
                      : "Show"}
                  </button>

                </div>

                {strength && (
                  <small className="strength">
                    Strength:{" "}
                    <strong>
                      {strength}
                    </strong>
                  </small>
                )}

                {editingId && (
                  <small>
                    Leave password empty to keep
                    the existing password.
                  </small>
                )}

              </div>

            </div>

            <div className="form-actions">

              <button
                className="primary-btn"
                type="submit"
              >
                {editingId
                  ? "Update Credential"
                  : "🔐 Save Credential"}
              </button>

              {editingId && (
                <button
                  className="cancel-btn"
                  type="button"
                  onClick={resetForm}
                >
                  Cancel
                </button>
              )}

            </div>

          </form>

          {message && (
            <div className="message">
              {message}
            </div>
          )}

        </section>

        {/* ===================================================
            SAVED CREDENTIALS
        =================================================== */}

        <section className="saved-section">

          <div className="saved-header">

            <div>

              <span className="section-label">
                YOUR CREDENTIALS
              </span>

              <h2>
                Saved Credentials
              </h2>

            </div>

            <div className="search-box">

              🔍

              <input
                type="text"
                placeholder="Search credentials..."
                value={search}
                onChange={(e) =>
                  setSearch(e.target.value)
                }
              />

            </div>

          </div>

          {filteredCredentials.length === 0 ? (

            <div className="empty-state">

              <div className="empty-icon">
                🔐
              </div>

              <h3>
                No credentials found
              </h3>

              <p>
                Add your first credential to
                your secure vault.
              </p>

            </div>

          ) : (

            <div className="credential-list">

              {filteredCredentials.map(
                (credential) => (

                  <div
                    className="credential-card"
                    key={credential.id}
                  >

                    <div className="credential-logo">
                      🔑
                    </div>

                    <div className="credential-info">

                      <h3>
                        {credential.title}
                      </h3>

                      <p>
                        {credential.username}
                      </p>

                      <div className="password-row">
                        Password: ••••••••
                      </div>

                    </div>

                    <div className="card-actions">

                      <button
                        className="edit-btn"
                        onClick={() =>
                          editCredential(
                            credential
                          )
                        }
                      >
                        ✏️ Edit
                      </button>

                      <button
                        className="delete-btn"
                        onClick={() =>
                          deleteCredential(
                            credential.id
                          )
                        }
                      >
                        🗑️ Delete
                      </button>

                    </div>

                  </div>
                )
              )}

            </div>
          )}

        </section>

        {/* ===================================================
            SHARING
        =================================================== */}

        <section className="credential-form share-section">

          <span className="section-label">
            SECURE SHARING
          </span>

          <h2>
            Share Credential
          </h2>

          <div className="form-grid">

            {/* SHARED USER */}

            <div className="input-group">

              <label>
                User Email
              </label>

              <input
                type="email"
                placeholder="user@example.com"
                value={sharedUserEmail}
                onChange={(e) =>
                  setSharedUserEmail(
                    e.target.value
                  )
                }
              />

            </div>

            {/* PERMISSION */}

            <div className="input-group">

              <label>
                Permission
              </label>

              <select
                value={permission}
                onChange={(e) =>
                  setPermission(
                    e.target.value
                  )
                }
              >

                <option value="VIEW_ONLY">
                  View Only
                </option>

                <option value="EDIT">
                  Edit
                </option>

                <option value="FULL_MANAGEMENT">
                  Full Management
                </option>

              </select>

            </div>

            {/* EXPIRY */}

            <div className="input-group">

              <label>
                Expires At
              </label>

              <input
                type="datetime-local"
                value={expiresAt}
                onChange={(e) =>
                  setExpiresAt(
                    e.target.value
                  )
                }
              />

            </div>

          </div>

          <div className="credential-list share-list">

            {credentials.map(
              (credential) => (

                <div
                  className="credential-card"
                  key={`share-${credential.id}`}
                >

                  <div className="credential-logo">
                    🔗
                  </div>

                  <div className="credential-info">

                    <h3>
                      {credential.title}
                    </h3>

                    <p>
                      {credential.username}
                    </p>

                  </div>

                  <button
                    className="share-btn"
                    onClick={() =>
                      shareCredential(
                        credential.id
                      )
                    }
                  >
                    🔗 Share
                  </button>

                </div>
              )
            )}

          </div>

        </section>

      </main>

    </div>
  );
}

export default Vault;