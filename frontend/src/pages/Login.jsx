import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "../styles/Auth.css";

function Login() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  // MFA
  const [otp, setOtp] = useState("");
  const [mfaToken, setMfaToken] = useState("");
  const [showMfa, setShowMfa] = useState(false);

  // UI
  const [showPassword, setShowPassword] = useState(false);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  // =========================================
  // GET / CREATE DEVICE ID
  // =========================================

  const getDeviceId = () => {
    try {
      let deviceId = localStorage.getItem(
        "securevault_device_id"
      );

      if (!deviceId) {
        deviceId = crypto.randomUUID();

        localStorage.setItem(
          "securevault_device_id",
          deviceId
        );
      }

      return deviceId;

    } catch (error) {

      console.error(
        "DEVICE ID ERROR:",
        error
      );

      return null;
    }
  };


  // =========================================
  // NORMAL LOGIN
  // =========================================

  const handleLogin = async (e) => {
    e.preventDefault();

    if (!email.trim() || !password.trim()) {
      setMessage(
        "Please enter email and password."
      );
      return;
    }

    setLoading(true);
    setMessage("");

    try {

      // -----------------------------------------
      // GET PERSISTENT DEVICE ID
      // -----------------------------------------

      const deviceId = getDeviceId();

      if (!deviceId) {
        setMessage(
          "Unable to identify this device. Please try again."
        );
        return;
      }


      const response = await fetch(
        "http://localhost:8080/api/auth/login",
        {
          method: "POST",

          headers: {
            "Content-Type": "application/json",
            "X-Device-Id": deviceId,
          },

          body: JSON.stringify({
            email: email.trim(),
            password: password,
          }),
        }
      );


      // -----------------------------------------
      // SAFELY HANDLE JSON / TEXT RESPONSE
      // -----------------------------------------

      const contentType =
        response.headers.get(
          "content-type"
        ) || "";

      let data;

      if (
        contentType.includes(
          "application/json"
        )
      ) {
        data = await response.json();
      } else {
        data = await response.text();
      }


      console.log(
        "LOGIN STATUS:",
        response.status
      );

      console.log(
        "LOGIN RESPONSE:",
        data
      );


      // =========================================
      // LOGIN FAILED
      // =========================================

      if (!response.ok) {

        setMessage(
          typeof data === "string"
            ? data
            : data?.message ||
                "Invalid email or password."
        );

        return;
      }


      // =========================================
      // MFA REQUIRED
      // =========================================

      if (
        typeof data === "object" &&
        data.mfaRequired === true
      ) {

        if (!data.mfaToken) {

          setMessage(
            "MFA is required, but MFA token was not received."
          );

          return;
        }

        setMfaToken(
          data.mfaToken
        );

        setShowMfa(true);

        setMessage(
          "MFA verification required."
        );

        return;
      }


      // =========================================
      // NORMAL LOGIN SUCCESS
      // =========================================

      if (
        typeof data !== "object" ||
        !data.token
      ) {

        setMessage(
          "Login successful, but token was not received."
        );

        return;
      }


      // -----------------------------------------
      // SAVE JWT
      // -----------------------------------------

      localStorage.setItem(
        "token",
        data.token
      );

      localStorage.setItem(
        "email",
        data.email || email.trim()
      );


      // -----------------------------------------
      // CLEANUP
      // -----------------------------------------

      sessionStorage.removeItem(
        "resetToken"
      );


      setMessage(
        "Login successful!"
      );

      navigate("/vault");


    } catch (error) {

      console.error(
        "LOGIN ERROR:",
        error
      );

      setMessage(
        "Unable to connect to backend."
      );

    } finally {

      setLoading(false);
    }
  };


  // =========================================
  // MFA VERIFY
  // =========================================

  const handleMfaVerify = async (e) => {

    e.preventDefault();

    if (!otp.trim()) {

      setMessage(
        "Please enter the 6-digit MFA code."
      );

      return;
    }


    if (!/^\d{6}$/.test(otp)) {

      setMessage(
        "MFA code must contain 6 digits."
      );

      return;
    }


    if (!mfaToken) {

      setMessage(
        "MFA session expired. Please login again."
      );

      setShowMfa(false);

      return;
    }


    setLoading(true);
    setMessage("");


    try {

      // -----------------------------------------
      // GET SAME DEVICE ID
      // -----------------------------------------

      const deviceId = getDeviceId();

      if (!deviceId) {

        setMessage(
          "Unable to identify this device. Please login again."
        );

        return;
      }


      const response = await fetch(
        `http://localhost:8080/api/mfa/verify?code=${encodeURIComponent(
          otp
        )}&mfaToken=${encodeURIComponent(
          mfaToken
        )}`,
        {
          method: "POST",

          headers: {
            "X-Device-Id": deviceId,
          },
        }
      );


      // -----------------------------------------
      // RESPONSE
      // -----------------------------------------

      const contentType =
        response.headers.get(
          "content-type"
        ) || "";

      let data;

      if (
        contentType.includes(
          "application/json"
        )
      ) {

        data = await response.json();

      } else {

        data = await response.text();
      }


      console.log(
        "MFA STATUS:",
        response.status
      );

      console.log(
        "MFA RESPONSE:",
        data
      );


      // =========================================
      // MFA FAILED
      // =========================================

      if (!response.ok) {

        setMessage(
          typeof data === "string"
            ? data
            : data?.message ||
                "Invalid MFA code."
        );

        return;
      }


      // =========================================
      // MFA SUCCESS
      // =========================================

      if (
        typeof data !== "object" ||
        !data.token
      ) {

        setMessage(
          "MFA verified, but login token was not received."
        );

        return;
      }


      // -----------------------------------------
      // SAVE FINAL JWT
      // -----------------------------------------

      localStorage.setItem(
        "token",
        data.token
      );

      localStorage.setItem(
        "email",
        data.email || email.trim()
      );


      setMessage(
        "MFA verification successful!"
      );


      setShowMfa(false);
      setOtp("");
      setMfaToken("");


      navigate("/vault");


    } catch (error) {

      console.error(
        "MFA ERROR:",
        error
      );

      setMessage(
        "Unable to verify MFA."
      );

    } finally {

      setLoading(false);
    }
  };


  // =========================================
  // BACK TO NORMAL LOGIN
  // =========================================

  const handleBackToLogin = () => {

    setShowMfa(false);
    setOtp("");
    setMfaToken("");
    setMessage("");

  };


  // =========================================
  // UI
  // =========================================

  return (
    <div className="auth-page">

      {/* =====================================
          LEFT SIDE
      ====================================== */}

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
            Your passwords.
            <br />
            <span>Secured.</span>
          </h1>

          <p>
            Store, manage and share your
            credentials securely with
            enterprise-grade protection.
          </p>


          <div className="security-features">

            <div>
              <span>✓</span>
              AES-256 encrypted vault
            </div>

            <div>
              <span>✓</span>
              Secure credential sharing
            </div>

            <div>
              <span>✓</span>
              Password strength analysis
            </div>

            <div>
              <span>✓</span>
              JWT authenticated access
            </div>

            <div>
              <span>✓</span>
              Multi-Factor Authentication
            </div>

          </div>

        </div>


        <div className="auth-footer">
          © 2026 SecureVault. All rights reserved.
        </div>

      </div>


      {/* =====================================
          RIGHT SIDE
      ====================================== */}

      <div className="auth-right">

        <div className="auth-card">

          {/* Mobile brand */}

          <div className="mobile-brand">

            <div className="auth-logo">
              🔐
            </div>

            <h2>
              SecureVault
            </h2>

          </div>


          {/* =================================
              NORMAL LOGIN
          ================================= */}

          {!showMfa ? (

            <>

              <div className="auth-heading">

                <span className="auth-label">
                  WELCOME BACK
                </span>

                <h1>
                  Sign in to your vault
                </h1>

                <p>
                  Enter your credentials to
                  access your secure password
                  vault.
                </p>

              </div>


              <form
                onSubmit={handleLogin}
              >

                {/* EMAIL */}

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
                        setEmail(
                          e.target.value
                        )
                      }
                      autoComplete="email"
                      disabled={loading}
                    />

                  </div>

                </div>


                {/* PASSWORD */}

                <div className="auth-input-group">

                  <div className="password-label-row">

                    <label>
                      Password
                    </label>


                    <button
                      type="button"
                      className="forgot-btn"
                      onClick={() =>
                        navigate(
                          "/forgot-password"
                        )
                      }
                    >
                      Forgot password?
                    </button>

                  </div>


                  <div className="auth-input-wrapper">

                    <span>
                      🔒
                    </span>


                    <input
                      type={
                        showPassword
                          ? "text"
                          : "password"
                      }
                      placeholder="Enter your password"
                      value={password}
                      onChange={(e) =>
                        setPassword(
                          e.target.value
                        )
                      }
                      autoComplete="current-password"
                      disabled={loading}
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
                      {showPassword
                        ? "Hide"
                        : "Show"}
                    </button>

                  </div>

                </div>


                {/* LOGIN BUTTON */}

                <button
                  type="submit"
                  className="auth-submit"
                  disabled={loading}
                >
                  {loading
                    ? "Signing in..."
                    : "Sign in to SecureVault →"}
                </button>

              </form>

            </>

          ) : (

            /* =================================
               MFA SCREEN
            ================================= */

            <>

              <div className="auth-heading">

                <span className="auth-label">
                  MFA VERIFICATION
                </span>

                <h1>
                  Verify your identity
                </h1>

                <p>
                  Enter the 6-digit code from
                  your authenticator app.
                </p>

              </div>


              <form
                onSubmit={handleMfaVerify}
              >

                <div className="auth-input-group">

                  <label>
                    Authentication code
                  </label>

                  <div className="auth-input-wrapper">

                    <span>
                      🔐
                    </span>

                    <input
                      type="text"
                      inputMode="numeric"
                      maxLength={6}
                      placeholder="123456"
                      value={otp}
                      onChange={(e) =>
                        setOtp(
                          e.target.value.replace(
                            /\D/g,
                            ""
                          )
                        )
                      }
                      autoComplete="one-time-code"
                      disabled={loading}
                      autoFocus
                    />

                  </div>

                </div>


                <button
                  type="submit"
                  className="auth-submit"
                  disabled={loading}
                >
                  {loading
                    ? "Verifying..."
                    : "Verify MFA →"}
                </button>

              </form>


              <button
                type="button"
                className="forgot-btn"
                onClick={
                  handleBackToLogin
                }
                disabled={loading}
              >
                ← Back to login
              </button>

            </>

          )}


          {/* =================================
              MESSAGE
          ================================= */}

          {message && (

            <div
              className={`auth-message ${
                message
                  .toLowerCase()
                  .includes("successful")
                  ? "success"
                  : "error"
              }`}
            >
              {message}
            </div>

          )}


          {/* =================================
              SECURITY DIVIDER
          ================================= */}

          <div className="auth-divider">

            <span>
              SECURE ACCESS
            </span>

          </div>


          {/* =================================
              SECURITY NOTE
          ================================= */}

          <div className="security-note">

            <span>
              🛡️
            </span>

            <div>

              <strong>
                Your data is protected
              </strong>

              <p>
                Secure authentication and
                encrypted credential storage.
              </p>

            </div>

          </div>


          {/* =================================
              REGISTER
          ================================= */}

          <p className="auth-switch">

            Don't have an account?{" "}

            <Link to="/register">
              Create an account
            </Link>

          </p>

        </div>

      </div>

    </div>
  );
}

export default Login;