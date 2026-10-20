import React from "react";
import { useLocation, useNavigate } from "react-router-dom";

function Sidebar() {
  const navigate = useNavigate();
  const location = useLocation();

  const menuItems = [
    {
      path: "/vault",
      icon: "🔑",
      label: "Vault",
    },
    {
      path: "/shared-vault",
      icon: "🔗",
      label: "Shared Vault",
    },
    {
      path: "/team-vault",
      icon: "👥",
      label: "Team Vault",
    },
    {
      path: "/analytics",
      icon: "📊",
      label: "Security Analytics",
    },
    {
      path: "/reports",
      icon: "📑",
      label: "Reports",
    },
    {
      path: "/settings",
      icon: "⚙️",
      label: "Settings",
    },
  ];

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("email");
    localStorage.removeItem("role");

    window.location.href = "/login";
  };

  return (
    <aside className="app-sidebar">

      <div className="sidebar-brand">

        <div className="sidebar-brand-icon">
          🔐
        </div>

        <strong>
          Secure & Protected
        </strong>

        <p>
          AES-256 encrypted storage
        </p>

      </div>

      {/* =========================
          NAVIGATION
      ========================= */}

      <nav className="sidebar-navigation">

        {menuItems.map((item) => {

          const isActive =
            location.pathname === item.path;

          return (
            <button
              key={item.path}
              type="button"
              className={`sidebar-menu-item ${
                isActive ? "active" : ""
              }`}
              onClick={() => navigate(item.path)}
            >
              <span className="sidebar-menu-icon">
                {item.icon}
              </span>

              <span className="sidebar-menu-label">
                {item.label}
              </span>
            </button>
          );
        })}

      </nav>

      {}

      <button
        type="button"
        className="sidebar-logout"
        onClick={logout}
      >
        <span>↪</span>
        Logout
      </button>

    </aside>
  );
}

export default Sidebar;