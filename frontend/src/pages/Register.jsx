import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "../styles/Auth.css";

function Register() {
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] =
    useState("");

  const [showPassword, setShowPassword] =
    useState(false);

  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  const handleRegister = async (e) => {
    e.preventDefault();

    if (
      !name.trim() ||
      !email.trim() ||
      !password.trim() ||
      !confirmPassword.trim()
    ) {
      setMessage("Please fill all fields.");
      return;
    }

    if (password !== confirmPassword) {
      setMessage("Passwords do not match.");
      return;
    }

    if (password.length < 8) {
      setMessage(
        "Password must contain at least 8 characters."
      );
      return;
    }

    setLoading(true);
    setMessage("");

    try {
      const response = await fetch(
        "http://localhost:8080/api/auth/register",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            name: name.trim(),
            email: email.trim(),
            password,
          }),
        }
      );

      const data = await response.text();

      if (response.ok) {
        setMessage(
          "Account created successfully! Redirecting..."
        );

        setTimeout(() => {
          navigate("/login");
        }, 1000);
      } else {
        setMessage(
          data || "Registration failed."
        );
      }
    } catch {
      setMessage("Unable to connect to backend.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">

      <div className="auth-left">

        <div className="auth-brand">
          <div className="auth-logo">🔐</div>

          <div>
            <h2>SecureVault</h2>
            <span>Enterprise Password Security</span>
          </div>
        </div>

        <div className="auth-hero">

          <div className="security-icon">
            🚀
          </div>

          <h1>
            Build your
            <br />
            <span>secure vault.</span>
          </h1>

          <p>
            Create one secure place for all your
            passwords and digital credentials.
          </p>

          <div className="security-features">

            <div>
              <span>✓</span>
              Secure credential management
            </div>

            <div>
              <span>✓</span>
              Strong password generator
            </div>

            <div>
              <span>✓</span>
              Controlled credential sharing
            </div>

            <div>
              <span>✓</span>
              Enterprise-ready security
            </div>

          </div>

        </div>

        <div className="auth-footer">
          © 2026 SecureVault. All rights reserved.
        </div>

      </div>

      <div className="auth-right">

        <div className="auth-card register-card">

          <div className="mobile-brand">
            <div className="auth-logo">🔐</div>
            <h2>SecureVault</h2>
          </div>

          <div className="auth-heading">

            <span className="auth-label">
              GET STARTED
            </span>

            <h1>Create your account</h1>

            <p>
              Set up your secure password vault in
              just a few steps.
            </p>

          </div>

          <form onSubmit={handleRegister}>

            <div className="auth-input-group">

              <label>Full name</label>

              <div className="auth-input-wrapper">

                <span>👤</span>

                <input
                  type="text"
                  placeholder="Your full name"
                  value={name}
                  onChange={(e) =>
                    setName(e.target.value)
                  }
                  autoComplete="name"
                />

              </div>

            </div>

            <div className="auth-input-group">

              <label>Email address</label>

              <div className="auth-input-wrapper">

                <span>✉️</span>

                <input
                  type="email"
                  placeholder="you@example.com"
                  value={email}
                  onChange={(e) =>
                    setEmail(e.target.value)
                  }
                  autoComplete="email"
                />

              </div>

            </div>

            <div className="auth-input-group">

              <label>Password</label>

              <div className="auth-input-wrapper">

                <span>🔒</span>

                <input
                  type={
                    showPassword
                      ? "text"
                      : "password"
                  }
                  placeholder="Create a strong password"
                  value={password}
                  onChange={(e) =>
                    setPassword(e.target.value)
                  }
                  autoComplete="new-password"
                />

                <button
                  type="button"
                  className="password-toggle"
                  onClick={() =>
                    setShowPassword(
                      !showPassword
                    )
                  }
                >
                  {showPassword ? "Hide" : "Show"}
                </button>

              </div>

              <small className="input-hint">
                Use at least 8 characters with a
                combination of letters and numbers.
              </small>

            </div>

            <div className="auth-input-group">

              <label>Confirm password</label>

              <div className="auth-input-wrapper">

                <span>🔐</span>

                <input
                  type="password"
                  placeholder="Confirm your password"
                  value={confirmPassword}
                  onChange={(e) =>
                    setConfirmPassword(
                      e.target.value
                    )
                  }
                  autoComplete="new-password"
                />

              </div>

            </div>

            <button
              type="submit"
              className="auth-submit"
              disabled={loading}
            >
              {loading
                ? "Creating account..."
                : "Create SecureVault Account →"}
            </button>

          </form>

          {message && (
            <div
              className={`auth-message ${
                message.includes("successfully")
                  ? "success"
                  : "error"
              }`}
            >
              {message}
            </div>
          )}

          <div className="security-note">

            <span>🛡️</span>

            <div>
              <strong>Security first</strong>

              <p>
                Your credentials are protected using
                secure authentication and encryption.
              </p>
            </div>

          </div>

          <p className="auth-switch">
            Already have an account?{" "}
            <Link to="/login">
              Sign in
            </Link>
          </p>

        </div>

      </div>

    </div>
  );
}

export default Register;