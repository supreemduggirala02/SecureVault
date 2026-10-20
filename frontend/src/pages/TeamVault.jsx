import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../App.css";

function TeamVault() {
  const navigate = useNavigate();

  const [teamName, setTeamName] = useState("");
  const [memberEmail, setMemberEmail] = useState("");
  const [permission, setPermission] = useState("VIEW_ONLY");
  const [message, setMessage] = useState("");

  const [members, setMembers] = useState([]);

  const token = localStorage.getItem("token");

  const logout = () => {
    localStorage.removeItem("token");
    navigate("/login");
  };

  const addMember = () => {
    if (!memberEmail.trim()) {
      setMessage("Please enter team member email");
      return;
    }

    const alreadyExists = members.some(
      (member) =>
        member.email.toLowerCase() ===
        memberEmail.trim().toLowerCase()
    );

    if (alreadyExists) {
      setMessage("This user is already a team member");
      return;
    }

    const newMember = {
      id: Date.now(),
      email: memberEmail.trim(),
      permission,
      status: "Active",
    };

    setMembers((previous) => [
      ...previous,
      newMember,
    ]);

    setMemberEmail("");
    setPermission("VIEW_ONLY");

    setMessage("Team member added successfully");
  };

  const removeMember = (id) => {
    const confirmed = window.confirm(
      "Remove this member from the team?"
    );

    if (!confirmed) {
      return;
    }

    setMembers((previous) =>
      previous.filter((member) => member.id !== id)
    );

    setMessage("Team member removed");
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
            className="nav-item"
            onClick={() => navigate("/shared-vault")}
          >
            <span>🔗</span>
            Shared Vault
          </div>

          <div
            className="nav-item active"
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

      {/* MAIN */}

      <main className="main-content">

        {/* TOPBAR */}

        <header className="topbar">

          <div>

            <p className="welcome-small">
              Workspace Management
            </p>

            <h1>
              Team Vault
            </h1>

          </div>

          <button
            className="logout-btn"
            onClick={logout}
          >
            Logout
          </button>

        </header>

        {/* TEAM OVERVIEW */}

        <section className="credential-form">

          <div className="section-heading">

            <div>

              <span className="section-label">
                TEAM WORKSPACE
              </span>

              <h2>
                Secure team collaboration
              </h2>

              <p>
                Manage team members and control their
                access to shared credentials.
              </p>

            </div>

          </div>

        </section>

        {/* STATS */}

        <section className="stats">

          <div className="stat-card">

            <span className="stat-icon">
              👥
            </span>

            <div>

              <p>
                Team Members
              </p>

              <h2>
                {members.length}
              </h2>

            </div>

          </div>

          <div className="stat-card">

            <span className="stat-icon">
              🟢
            </span>

            <div>

              <p>
                Active Members
              </p>

              <h2>
                {
                  members.filter(
                    (member) =>
                      member.status === "Active"
                  ).length
                }
              </h2>

            </div>

          </div>

          <div className="stat-card">

            <span className="stat-icon">
              🔐
            </span>

            <div>

              <p>
                Protected
              </p>

              <h2>
                AES-256
              </h2>

            </div>

          </div>

        </section>

        {/* CREATE TEAM */}

        <section className="credential-form">

          <div className="section-heading">

            <div>

              <span className="section-label">
                TEAM SETTINGS
              </span>

              <h2>
                Team information
              </h2>

            </div>

          </div>

          <div className="form-grid">

            <div className="input-group">

              <label>
                Team / Workspace Name
              </label>

              <input
                type="text"
                placeholder="e.g. Development Team"
                value={teamName}
                onChange={(e) =>
                  setTeamName(e.target.value)
                }
              />

            </div>

            <div className="input-group">

              <label>
                Workspace Security
              </label>

              <input
                type="text"
                value="AES-256 Encrypted"
                disabled
              />

            </div>

            <div className="input-group">

              <label>
                Access Policy
              </label>

              <input
                type="text"
                value="Role Based Access Control"
                disabled
              />

            </div>

          </div>

          <div className="form-actions">

            <button
              className="primary-btn"
              type="button"
              onClick={() =>
                setMessage(
                  "Team settings saved successfully"
                )
              }
            >
              ✓ Save Team Settings
            </button>

          </div>

        </section>

        {/* ADD MEMBER */}

        <section className="credential-form">

          <div className="section-heading">

            <div>

              <span className="section-label">
                TEAM MEMBERS
              </span>

              <h2>
                Add team member
              </h2>

            </div>

          </div>

          <div className="form-grid">

            <div className="input-group">

              <label>
                Member Email
              </label>

              <input
                type="email"
                placeholder="developer@example.com"
                value={memberEmail}
                onChange={(e) =>
                  setMemberEmail(e.target.value)
                }
              />

            </div>

            <div className="input-group">

              <label>
                Permission
              </label>

              <select
                value={permission}
                onChange={(e) =>
                  setPermission(e.target.value)
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

          </div>

          <div className="form-actions">

            <button
              className="primary-btn"
              type="button"
              onClick={addMember}
            >
              + Add Member
            </button>

          </div>

        </section>

        {/* MESSAGE */}

        {message && (
          <div className="message">
            {message}
          </div>
        )}

        {/* MEMBERS LIST */}

        <section className="saved-section">

          <div className="saved-header">

            <div>

              <span className="section-label">
                MEMBERS
              </span>

              <h2>
                Team Members
              </h2>

            </div>

          </div>

          {members.length === 0 ? (

            <div className="empty-state">

              <div className="empty-icon">
                👥
              </div>

              <h3>
                No team members
              </h3>

              <p>
                Add users to start collaborating
                securely with your team.
              </p>

            </div>

          ) : (

            <div className="credential-list">

              {members.map((member) => (

                <div
                  className="credential-card"
                  key={member.id}
                >

                  <div className="credential-logo">
                    👤
                  </div>

                  <div className="credential-info">

                    <h3>
                      {member.email}
                    </h3>

                    <p>
                      Permission:{" "}
                      <strong>
                        {member.permission}
                      </strong>
                    </p>

                    <p>
                      Status:{" "}
                      <strong>
                        🟢 {member.status}
                      </strong>
                    </p>

                  </div>

                  <div className="form-actions">

                    <button
                      className="cancel-btn"
                      type="button"
                      onClick={() =>
                        removeMember(member.id)
                      }
                    >
                      Remove
                    </button>

                  </div>

                </div>

              ))}

            </div>

          )}

        </section>

        {/* SECURITY FEATURES */}

        <section className="credential-form">

          <div className="section-heading">

            <div>

              <span className="section-label">
                SECURITY FEATURES
              </span>

              <h2>
                Enterprise-grade protection
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
                  AES-256 Encryption
                </h3>

                <p>
                  Sensitive credential data is
                  protected using strong encryption.
                </p>

              </div>

            </div>

            <div className="stat-card">

              <span className="stat-icon">
                🛡️
              </span>

              <div>

                <h3>
                  Role-Based Access
                </h3>

                <p>
                  Control what each team member
                  can view or modify.
                </p>

              </div>

            </div>

            <div className="stat-card">

              <span className="stat-icon">
                📋
              </span>

              <div>

                <h3>
                  Audit Ready
                </h3>

                <p>
                  Team access can be monitored
                  through security logs.
                </p>

              </div>

            </div>

          </div>

        </section>

      </main>

    </div>
  );
}

export default TeamVault;