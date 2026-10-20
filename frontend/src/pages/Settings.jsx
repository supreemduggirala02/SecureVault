import { useEffect, useState } from "react";
import QRCode from "qrcode";
import { useNavigate } from "react-router-dom";
import "../styles/setting.css";

function Settings() {
  const navigate = useNavigate();

  const [mfaEnabled, setMfaEnabled] = useState(false);
  const [otpUri, setOtpUri] = useState("");
  const [qrImage, setQrImage] = useState("");
  const [secret, setSecret] = useState("");
  const [otp, setOtp] = useState("");

  const [showSetup, setShowSetup] = useState(false);
  const [loading, setLoading] = useState(false);

  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("error");

  // ==========================================
  // SHOW MESSAGE
  // ==========================================

  const showMessage = (text, type = "error") => {
    setMessage(text);
    setMessageType(type);
  };

  // ==========================================
  // GET LOGIN DETAILS
  // ==========================================

  const getAuthData = () => {
    return {
      email: localStorage.getItem("email"),
      token: localStorage.getItem("token"),
    };
  };

  // ==========================================
  // LOAD MFA STATUS
  // ==========================================

  useEffect(() => {
    const savedStatus = localStorage.getItem("mfaEnabled");

    if (savedStatus === "true") {
      setMfaEnabled(true);
    }
  }, []);

  // ==========================================
  // GENERATE QR CODE
  // ==========================================

  const generateQRCode = async (uri) => {
    try {
      const image = await QRCode.toDataURL(uri, {
        width: 220,
        margin: 2,
        errorCorrectionLevel: "M",
      });

      setQrImage(image);
    } catch (error) {
      console.error("QR CODE ERROR:", error);
      showMessage("Unable to generate QR code.");
    }
  };

  // ==========================================
  // MFA SETUP
  // ==========================================

  const setupMFA = async () => {
    const { email, token } = getAuthData();

    if (!email || !token) {
      showMessage("Please login again.");
      return;
    }

    setLoading(true);
    setMessage("");
    setQrImage("");
    setOtpUri("");
    setSecret("");
    setOtp("");

    try {
      const response = await fetch(
        `http://localhost:8080/api/mfa/setup?email=${encodeURIComponent(
          email
        )}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
            Accept: "application/json, text/plain, */*",
          },
        }
      );

      const text = await response.text();

      console.log("MFA SETUP STATUS:", response.status);
      console.log("MFA SETUP RESPONSE:", text);

      let data = {};

      try {
        data = text ? JSON.parse(text) : {};
      } catch {
        data = {
          message: text,
        };
      }

      // ======================================
      // BACKEND ERROR
      // ======================================

      if (!response.ok) {
        const errorMessage =
          data.message ||
          text ||
          "MFA setup failed.";

        console.error(
          "MFA SETUP ERROR:",
          errorMessage
        );

        // MFA already enabled
        if (
          errorMessage
            .toLowerCase()
            .includes("already enabled")
        ) {
          setMfaEnabled(true);

          localStorage.setItem(
            "mfaEnabled",
            "true"
          );

          showMessage(
            "MFA is already enabled for your account.",
            "success"
          );

          return;
        }

        showMessage(errorMessage);
        return;
      }

      // ======================================
      // SUCCESS
      // ======================================

      if (!data.otpUri) {
        showMessage(
          "MFA setup succeeded, but QR information was not received."
        );
        return;
      }

      setOtpUri(data.otpUri);
      setSecret(data.secret || "");

      await generateQRCode(data.otpUri);

      setShowSetup(true);

      showMessage(
        "Scan the QR code using Google Authenticator.",
        "success"
      );

    } catch (error) {
      console.error("MFA SETUP ERROR:", error);

      showMessage(
        "Unable to connect to backend."
      );
    } finally {
      setLoading(false);
    }
  };

  // ==========================================
  // VERIFY MFA
  // ==========================================

  const verifyMFA = async () => {
    if (!/^\d{6}$/.test(otp)) {
      showMessage(
        "Please enter a valid 6-digit OTP."
      );
      return;
    }

    const { token } = getAuthData();

    if (!token) {
      showMessage("Please login again.");
      return;
    }

    setLoading(true);
    setMessage("");

    try {
      const response = await fetch(
        `http://localhost:8080/api/mfa/verify?code=${encodeURIComponent(
          otp
        )}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
            Accept: "application/json, text/plain, */*",
          },
        }
      );

      const text = await response.text();

      console.log("MFA VERIFY STATUS:", response.status);
      console.log("MFA VERIFY RESPONSE:", text);

      let data = {};

      try {
        data = text ? JSON.parse(text) : {};
      } catch {
        data = {
          message: text,
        };
      }

      if (!response.ok) {
        showMessage(
          data.message ||
            text ||
            "Invalid OTP."
        );
        return;
      }

      // ======================================
      // MFA ENABLED
      // ======================================

      setMfaEnabled(true);

      localStorage.setItem(
        "mfaEnabled",
        "true"
      );

      setShowSetup(false);
      setOtp("");
      setOtpUri("");
      setSecret("");
      setQrImage("");

      showMessage(
        "MFA enabled successfully!",
        "success"
      );

    } catch (error) {
      console.error(
        "MFA VERIFY ERROR:",
        error
      );

      showMessage(
        "Unable to verify MFA."
      );
    } finally {
      setLoading(false);
    }
  };

  // ==========================================
  // DISABLE MFA
  // ==========================================

  const disableMFA = () => {
    showMessage(
      "MFA disabling is not implemented yet."
    );
  };

  // ==========================================
  // UI
  // ==========================================

  return (
    <div className="settings-page">

      {/* ======================================
          NAVBAR
      ====================================== */}

      <nav className="settings-navbar">

        <div className="settings-brand">

          <div className="brand-icon">
            🛡️
          </div>

          <div>
            <h2>SecureVault</h2>

            <span>
              Enterprise Password Security
            </span>
          </div>

        </div>

        <button
          className="back-vault-btn"
          onClick={() => navigate("/vault")}
        >
          ← Back to Vault
        </button>

      </nav>


      {/* ======================================
          MAIN
      ====================================== */}

      <main className="settings-container">

        {/* ====================================
            TITLE
        ==================================== */}

        <div className="settings-title">

          <div>

            <div className="page-label">
              SECURITY CENTER
            </div>

            <h1>
              Security Settings
            </h1>

            <p>
              Manage your account security and
              authentication preferences.
            </p>

          </div>

          <div className="security-score">

            <div className="score-icon">
              🛡️
            </div>

            <div>

              <span>
                SECURITY STATUS
              </span>

              <strong>
                {mfaEnabled
                  ? "Protected"
                  : "Needs Attention"}
              </strong>

            </div>

          </div>

        </div>


        {/* ====================================
            OVERVIEW CARDS
        ==================================== */}

        <div className="overview-grid">

          <div className="overview-card">

            <div className="overview-icon">
              🔑
            </div>

            <div>
              <span>
                Password Security
              </span>

              <strong>
                Active
              </strong>
            </div>

            <div className="status-dot active" />

          </div>


          <div className="overview-card">

            <div className="overview-icon">
              🛡️
            </div>

            <div>
              <span>
                Multi-Factor Authentication
              </span>

              <strong>
                {mfaEnabled
                  ? "Enabled"
                  : "Disabled"}
              </strong>
            </div>

            <div
              className={
                mfaEnabled
                  ? "status-dot active"
                  : "status-dot warning"
              }
            />

          </div>


          <div className="overview-card">

            <div className="overview-icon">
              🔐
            </div>

            <div>
              <span>
                Vault Encryption
              </span>

              <strong>
                AES-256
              </strong>
            </div>

            <div className="status-dot active" />

          </div>

        </div>


        {/* ====================================
            MFA CARD
        ==================================== */}

        <section className="security-card">

          <div className="security-card-header">

            <div className="security-card-title">

              <div className="large-security-icon">
                🛡️
              </div>

              <div>

                <h2>
                  Multi-Factor Authentication
                </h2>

                <p>
                  Add an additional layer of
                  protection to your SecureVault
                  account.
                </p>

              </div>

            </div>


            <div
              className={
                mfaEnabled
                  ? "mfa-badge enabled"
                  : "mfa-badge disabled"
              }
            >

              <span>●</span>

              {mfaEnabled
                ? "Enabled"
                : "Disabled"}

            </div>

          </div>


          {/* ==================================
              MFA DESCRIPTION
          ================================== */}

          <div className="mfa-main-content">

            <div className="mfa-description">

              <h3>
                Protect your account
              </h3>

              <p>
                Multi-Factor Authentication requires
                a time-based verification code in
                addition to your password.
              </p>


              <div className="benefit-list">

                <div>
                  <span>✓</span>
                  Protection against stolen passwords
                </div>

                <div>
                  <span>✓</span>
                  Time-based one-time passwords
                </div>

                <div>
                  <span>✓</span>
                  Google Authenticator support
                </div>

                <div>
                  <span>✓</span>
                  Stronger protection for your vault
                </div>

              </div>

            </div>


            {/* ==================================
                BUTTON
            ================================== */}

            <div className="mfa-action">

              {!mfaEnabled ? (

                <button
                  className="primary-security-btn"
                  onClick={setupMFA}
                  disabled={loading}
                >

                  {loading
                    ? "Setting up..."
                    : "🛡 Enable MFA"}

                </button>

              ) : (

                <div className="protected-box">

                  <div className="protected-check">
                    ✓
                  </div>

                  <div>

                    <strong>
                      MFA Protected
                    </strong>

                    <p>
                      Your account has
                      additional protection.
                    </p>

                  </div>

                </div>

              )}

            </div>

          </div>


          {/* ==================================
              MFA SETUP
          ================================== */}

          {showSetup && otpUri && (

            <div className="mfa-setup">

              {/* STEP 1 */}

              <div className="setup-header">

                <div className="setup-number">
                  1
                </div>

                <div>

                  <h3>
                    Scan QR Code
                  </h3>

                  <p>
                    Open Google Authenticator
                    and scan the QR code below.
                  </p>

                </div>

              </div>


              {/* QR SECTION */}

              <div className="setup-content">

                <div className="qr-section">

                  <div className="qr-frame">

                    {qrImage ? (

                      <img
                        src={qrImage}
                        alt="MFA QR Code"
                      />

                    ) : (

                      <div className="qr-loading">
                        Generating QR...
                      </div>

                    )}

                  </div>

                  <div className="qr-caption">
                    Scan with Google Authenticator
                  </div>

                </div>


                {/* MANUAL SECRET */}

                <div className="manual-section">

                  <div className="setup-number small">
                    2
                  </div>

                  <div>

                    <h3>
                      Manual Setup
                    </h3>

                    <p>
                      If you cannot scan the QR
                      code, enter this secret manually
                      in your authenticator app.
                    </p>

                    <div className="secret-box">
                      {secret ||
                        "Secret unavailable"}
                    </div>

                  </div>

                </div>

              </div>


              {/* ==================================
                  VERIFY OTP
              ================================== */}

              <div className="verification-section">

                <div className="setup-number">
                  3
                </div>

                <div className="verification-content">

                  <h3>
                    Verify Authentication Code
                  </h3>

                  <p>
                    Enter the 6-digit code displayed
                    in your Google Authenticator app.
                  </p>


                  <div className="otp-row">

                    <input
                      type="text"
                      inputMode="numeric"
                      maxLength="6"
                      placeholder="123456"
                      value={otp}
                      onChange={(e) => {
                        setOtp(
                          e.target.value.replace(
                            /\D/g,
                            ""
                          )
                        );
                      }}
                    />


                    <button
                      className="verify-btn"
                      onClick={verifyMFA}
                      disabled={
                        loading ||
                        otp.length !== 6
                      }
                    >

                      {loading
                        ? "Verifying..."
                        : "Verify & Enable"}

                    </button>

                  </div>

                </div>

              </div>

            </div>

          )}

        </section>


        {/* ====================================
            MESSAGE
        ==================================== */}

        {message && (

          <div
            className={`settings-message ${
              messageType === "success"
                ? "success"
                : "error"
            }`}
          >

            <span>
              {messageType === "success"
                ? "✓"
                : "!"}
            </span>

            {message}

          </div>

        )}


        {/* ====================================
            SECURITY INFO
        ==================================== */}

        <div className="security-info">

          <div className="info-icon">
            🔐
          </div>

          <div>

            <h3>
              Your security matters
            </h3>

            <p>
              SecureVault uses modern security
              technologies including JWT
              authentication, BCrypt password
              hashing, AES-256 encryption and
              Multi-Factor Authentication to
              protect your credentials.
            </p>

          </div>

        </div>

      </main>

    </div>
  );
}

export default Settings;