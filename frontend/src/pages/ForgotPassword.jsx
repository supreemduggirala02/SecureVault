import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/Auth.css";

function ForgotPassword() {

  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {

    e.preventDefault();

    setMessage("");
    setError("");

    if (!email.trim()) {
      setError("Please enter your email address.");
      return;
    }

    setLoading(true);

    try {

      const response = await fetch(
        "http://localhost:8080/api/auth/forgot-password",
        {
          method: "POST",

          headers: {
            "Content-Type": "application/json"
          },

          body: JSON.stringify({
            email: email.trim()
          })
        }
      );

      const data = await response.json();

      console.log(
        "FORGOT PASSWORD STATUS:",
        response.status
      );

      console.log(
        "FORGOT PASSWORD RESPONSE:",
        data
      );

      if (!response.ok) {

        setError(
          data.message ||
          "Unable to process password recovery."
        );

        return;
      }

      setMessage(data.message);

      /*
       * DEVELOPMENT TESTING
       *
       * Backend currently returns token
       * so we can test password reset.
       */
      if (data.resetToken) {

        sessionStorage.setItem(
          "resetToken",
          data.resetToken
        );

        setTimeout(() => {
          navigate("/reset-password");
        }, 1200);
      }

    } catch (error) {

      console.error(
        "FORGOT PASSWORD ERROR:",
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
            🛡️
          </div>

          <h1>
            Recover your
            <br />
            <span>account.</span>
          </h1>

          <p>
            Securely reset your SecureVault
            password without exposing your
            existing password.
          </p>

          <div className="security-features">

            <div>
              <span>✓</span>
              Secure recovery token
            </div>

            <div>
              <span>✓</span>
              Token expiration
            </div>

            <div>
              <span>✓</span>
              BCrypt password hashing
            </div>

            <div>
              <span>✓</span>
              Single-use reset token
            </div>

          </div>

        </div>

      </div>

      <div className="auth-right">

        <div className="auth-card">

          <div className="auth-heading">

            <span className="auth-label">
              PASSWORD RECOVERY
            </span>

            <h1>
              Forgot your password?
            </h1>

            <p>
              Enter your registered email
              address to recover your account.
            </p>

          </div>

          <form onSubmit={handleSubmit}>

            <div className="auth-input-group">

              <label>
                Email address
              </label>

              <div className="auth-input-wrapper">

                <span>
                  ✉️
                </span>

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

            <button
              type="submit"
              className="auth-submit"
              disabled={loading}
            >
              {loading
                ? "Processing..."
                : "Recover Password →"}
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

          <button
            type="button"
            className="forgot-btn"
            onClick={() =>
              navigate("/login")
            }
          >
            ← Back to login
          </button>

          <div className="auth-divider">
            <span>
              SECURE RECOVERY
            </span>
          </div>

          <div className="security-note">

            <span>
              🛡️
            </span>

            <div>

              <strong>
                Your password is never exposed
              </strong>

              <p>
                Your new password is securely
                hashed before being stored.
              </p>

            </div>

          </div>

        </div>

      </div>

    </div>
  );
}

export default ForgotPassword;