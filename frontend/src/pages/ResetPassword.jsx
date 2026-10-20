import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/Auth.css";

function ResetPassword() {

  const navigate = useNavigate();

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] =
    useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleReset = async (e) => {

    e.preventDefault();

    setMessage("");
    setError("");

    const token =
      sessionStorage.getItem("resetToken");

    if (!token) {
      setError(
        "Reset token is missing or expired."
      );
      return;
    }

    if (password.length < 8) {
      setError(
        "Password must contain at least 8 characters."
      );
      return;
    }

    if (password !== confirmPassword) {
      setError(
        "Passwords do not match."
      );
      return;
    }

    setLoading(true);

    try {

      const response = await fetch(
        "http://localhost:8080/api/auth/reset-password",
        {
          method: "POST",

          headers: {
            "Content-Type": "application/json"
          },

          body: JSON.stringify({
            token: token,
            newPassword: password
          })
        }
      );

      const data = await response.json();

      console.log(
        "RESET PASSWORD STATUS:",
        response.status
      );

      console.log(
        "RESET PASSWORD RESPONSE:",
        data
      );

      if (!response.ok) {

        setError(
          data.message ||
          "Password reset failed."
        );

        return;
      }

      sessionStorage.removeItem(
        "resetToken"
      );

      setMessage(
        "Password reset successful! Redirecting to login..."
      );

      setTimeout(() => {
        navigate("/login");
      }, 1800);

    } catch (error) {

      console.error(
        "RESET PASSWORD ERROR:",
        error
      );

      setError(
        "Unable to connect to backend."
      );

    } finally {

      setLoading(false);
    }
  };

  return (
    <div className="auth-page">

      <div className="auth-left">

        <div className="auth-brand">

          <div className="auth-logo">
            🔐
          </div>

          <div>
            <h2>SecureVault</h2>

            <span>
              Enterprise Password Security
            </span>
          </div>

        </div>

        <div className="auth-hero">

          <div className="security-icon">
            🔑
          </div>

          <h1>
            Create a new
            <br />
            <span>password.</span>
          </h1>

          <p>
            Choose a strong password to
            protect your SecureVault account.
          </p>

          <div className="security-features">

            <div>
              <span>✓</span>
              Minimum 8 characters
            </div>

            <div>
              <span>✓</span>
              BCrypt protected
            </div>

            <div>
              <span>✓</span>
              Previous password replaced
            </div>

          </div>

        </div>

      </div>

      <div className="auth-right">

        <div className="auth-card">

          <div className="auth-heading">

            <span className="auth-label">
              RESET PASSWORD
            </span>

            <h1>
              Create new password
            </h1>

            <p>
              Enter your new SecureVault
              password below.
            </p>

          </div>

          <form onSubmit={handleReset}>

            <div className="auth-input-group">

              <label>
                New password
              </label>

              <div className="auth-input-wrapper">

                <span>
                  🔒
                </span>

                <input
                  type="password"
                  placeholder="Enter new password"
                  value={password}
                  onChange={(e) =>
                    setPassword(e.target.value)
                  }
                  autoComplete="new-password"
                />

              </div>

            </div>

            <div className="auth-input-group">

              <label>
                Confirm password
              </label>

              <div className="auth-input-wrapper">

                <span>
                  🔒
                </span>

                <input
                  type="password"
                  placeholder="Confirm new password"
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
                ? "Resetting..."
                : "Reset Password →"}
            </button>

          </form>

          {message && (
            <div className="auth-message success">
              {message}
            </div>
          )}

          {error && (
            <div className="auth-message error">
              {error}
            </div>
          )}

          <div className="auth-divider">
            <span>
              SECURE PASSWORD
            </span>
          </div>

          <div className="security-note">

            <span>
              🛡️
            </span>

            <div>

              <strong>
                Password securely protected
              </strong>

              <p>
                SecureVault uses BCrypt to
                protect your password.
              </p>

            </div>

          </div>

        </div>

      </div>

    </div>
  );
}

export default ResetPassword;