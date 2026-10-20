import { useEffect, useState } from "react";

import {
  getSystemReport,
  getAuditReport,
  exportAuditCsv,
  exportAuditPdf,
} from "../services/reportService";

function Reports() {
  const getDefaultStartDate = () => {
    const date = new Date();
    date.setDate(date.getDate() - 30);

    return date.toISOString().split("T")[0];
  };

  const getDefaultEndDate = () => {
    return new Date().toISOString().split("T")[0];
  };

  const [systemReport, setSystemReport] = useState(null);

  const [auditLogs, setAuditLogs] = useState([]);

  const [startDate, setStartDate] = useState(
    getDefaultStartDate()
  );

  const [endDate, setEndDate] = useState(
    getDefaultEndDate()
  );

  const [loading, setLoading] = useState(true);
  const [auditLoading, setAuditLoading] = useState(false);

  const [error, setError] = useState("");
  const [exporting, setExporting] = useState("");

  const loadSystemReport = async () => {
    try {
      setLoading(true);
      setError("");

      const data = await getSystemReport();

      setSystemReport(data);
    } catch (err) {
      setError(
        err.message || "Failed to load system report"
      );
    } finally {
      setLoading(false);
    }
  };

  const loadAuditReport = async () => {
    try {
      setAuditLoading(true);
      setError("");

      const data = await getAuditReport(
        startDate,
        endDate
      );

      setAuditLogs(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(
        err.message || "Failed to load audit report"
      );
    } finally {
      setAuditLoading(false);
    }
  };

  useEffect(() => {
    loadSystemReport();
    loadAuditReport();
  }, []);

  const handleApplyFilter = async () => {
    if (!startDate || !endDate) {
      setError("Please select both start and end dates.");
      return;
    }

    if (startDate > endDate) {
      setError(
        "Start date cannot be after end date."
      );
      return;
    }

    await loadAuditReport();
  };

  const handleCsvExport = async () => {
    try {
      setExporting("csv");
      setError("");

      await exportAuditCsv(
        startDate,
        endDate
      );
    } catch (err) {
      setError(
        err.message || "CSV export failed"
      );
    } finally {
      setExporting("");
    }
  };

  const handlePdfExport = async () => {
    try {
      setExporting("pdf");
      setError("");

      await exportAuditPdf(
        startDate,
        endDate
      );
    } catch (err) {
      setError(
        err.message || "PDF export failed"
      );
    } finally {
      setExporting("");
    }
  };

  const formatDateTime = (value) => {
    if (!value) {
      return "-";
    }

    try {
      return new Date(value).toLocaleString();
    } catch {
      return value;
    }
  };

  if (loading) {
    return (
      <div className="reports-page">
        <div className="reports-loading">
          Loading reports...
        </div>
      </div>
    );
  }

  return (
    <div className="reports-page">

      <div className="reports-header">

        <div>
          <h1>Reports</h1>

          <p>
            SecureVault security and system reports
          </p>
        </div>

        <button
          className="reports-refresh-button"
          onClick={loadSystemReport}
        >
          Refresh
        </button>

      </div>

      {error && (
        <div className="reports-error">
          {error}
        </div>
      )}

      {/* =========================
          SYSTEM STATISTICS
      ========================= */}

      <section className="reports-stat-grid">

        <div className="report-stat-card">
          <span className="report-stat-label">
            Total Users
          </span>

          <strong>
            {systemReport?.totalUsers ?? 0}
          </strong>
        </div>

        <div className="report-stat-card">
          <span className="report-stat-label">
            Total Credentials
          </span>

          <strong>
            {systemReport?.totalCredentials ?? 0}
          </strong>
        </div>

        <div className="report-stat-card">
          <span className="report-stat-label">
            Total Shares
          </span>

          <strong>
            {systemReport?.totalShares ?? 0}
          </strong>
        </div>

        <div className="report-stat-card">
          <span className="report-stat-label">
            Active Shares
          </span>

          <strong>
            {systemReport?.activeShares ?? 0}
          </strong>
        </div>

        <div className="report-stat-card">
          <span className="report-stat-label">
            Notifications
          </span>

          <strong>
            {systemReport?.totalNotifications ?? 0}
          </strong>
        </div>

        <div className="report-stat-card">
          <span className="report-stat-label">
            Unread Notifications
          </span>

          <strong>
            {systemReport?.unreadNotifications ?? 0}
          </strong>
        </div>

        <div className="report-stat-card">
          <span className="report-stat-label">
            Audit Records
          </span>

          <strong>
            {systemReport?.totalAuditLogs ?? 0}
          </strong>
        </div>

      </section>

      {/* =========================
          AUDIT REPORT
      ========================= */}

      <section className="reports-section">

        <div className="reports-section-header">

          <div>
            <h2>Security Audit Report</h2>

            <p>
              View security activity for a selected date range.
            </p>
          </div>

        </div>

        {/* DATE FILTER */}

        <div className="reports-filter">

          <div className="reports-date-field">

            <label htmlFor="report-start-date">
              Start Date
            </label>

            <input
              id="report-start-date"
              type="date"
              value={startDate}
              onChange={(event) =>
                setStartDate(event.target.value)
              }
            />

          </div>

          <div className="reports-date-field">

            <label htmlFor="report-end-date">
              End Date
            </label>

            <input
              id="report-end-date"
              type="date"
              value={endDate}
              onChange={(event) =>
                setEndDate(event.target.value)
              }
            />

          </div>

          <button
            className="reports-filter-button"
            onClick={handleApplyFilter}
            disabled={auditLoading}
          >
            {auditLoading
              ? "Loading..."
              : "Apply Filter"}
          </button>

        </div>

        {/* EXPORT BUTTONS */}

        <div className="reports-export-actions">

          <button
            className="reports-csv-button"
            onClick={handleCsvExport}
            disabled={exporting !== ""}
          >
            {exporting === "csv"
              ? "Exporting..."
              : "Export CSV"}
          </button>

          <button
            className="reports-pdf-button"
            onClick={handlePdfExport}
            disabled={exporting !== ""}
          >
            {exporting === "pdf"
              ? "Exporting..."
              : "Export PDF"}
          </button>

        </div>

        {/* AUDIT TABLE */}

        <div className="reports-table-wrapper">

          {auditLoading ? (
            <div className="reports-empty">
              Loading audit records...
            </div>
          ) : auditLogs.length === 0 ? (
            <div className="reports-empty">
              No audit records found for the selected period.
            </div>
          ) : (
            <table className="reports-table">

              <thead>

                <tr>
                  <th>Date & Time</th>
                  <th>User</th>
                  <th>Action</th>
                  <th>Status</th>
                  <th>IP Address</th>
                  <th>Details</th>
                </tr>

              </thead>

              <tbody>

                {auditLogs.map((log) => (

                  <tr key={log.id}>

                    <td>
                      {formatDateTime(log.timestamp)}
                    </td>

                    <td>
                      {log.userEmail || "-"}
                    </td>

                    <td>
                      {log.action || "-"}
                    </td>

                    <td>
                      <span
                        className={
                          log.loginStatus === "SUCCESS"
                            ? "report-status-success"
                            : log.loginStatus === "FAILED"
                              ? "report-status-failed"
                              : "report-status-neutral"
                        }
                      >
                        {log.loginStatus || "-"}
                      </span>
                    </td>

                    <td>
                      {log.ipAddress || "-"}
                    </td>

                    <td>
                      {log.details || "-"}
                    </td>

                  </tr>

                ))}

              </tbody>

            </table>
          )}

        </div>

      </section>

    </div>
  );
}

export default Reports;