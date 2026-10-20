const API_BASE_URL = "http://localhost:8080";

const getToken = () => localStorage.getItem("token");

const getHeaders = () => {
  const token = getToken();

  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
};

const handleResponse = async (response) => {
  if (!response.ok) {
    let message = `Request failed with status ${response.status}`;

    try {
      const data = await response.json();
      message = data?.message || message;
    } catch {
      // Response was not JSON
    }

    throw new Error(message);
  }

  return response.json();
};

export const getSystemReport = async () => {
  const response = await fetch(
    `${API_BASE_URL}/api/reports/system`,
    {
      method: "GET",
      headers: getHeaders(),
    }
  );

  return handleResponse(response);
};

export const getAuditReport = async (startDate, endDate) => {
  const params = new URLSearchParams();

  if (startDate) {
    params.append("startDate", startDate);
  }

  if (endDate) {
    params.append("endDate", endDate);
  }

  const response = await fetch(
    `${API_BASE_URL}/api/reports/audit?${params.toString()}`,
    {
      method: "GET",
      headers: getHeaders(),
    }
  );

  return handleResponse(response);
};

const downloadFile = async (url, defaultFileName) => {
  const response = await fetch(url, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
  });

  if (!response.ok) {
    let message = `Export failed with status ${response.status}`;

    try {
      const data = await response.json();
      message = data?.message || message;
    } catch {
      // Response was not JSON
    }

    throw new Error(message);
  }

  const blob = await response.blob();

  const contentDisposition =
    response.headers.get("Content-Disposition");

  let fileName = defaultFileName;

  if (contentDisposition) {
    const match = contentDisposition.match(
      /filename="?([^"]+)"?/i
    );

    if (match?.[1]) {
      fileName = match[1];
    }
  }

  const downloadUrl = window.URL.createObjectURL(blob);

  const link = document.createElement("a");

  link.href = downloadUrl;
  link.download = fileName;

  document.body.appendChild(link);
  link.click();

  link.remove();

  window.URL.revokeObjectURL(downloadUrl);
};

export const exportAuditCsv = async (
  startDate,
  endDate
) => {
  const params = new URLSearchParams();

  if (startDate) {
    params.append("startDate", startDate);
  }

  if (endDate) {
    params.append("endDate", endDate);
  }

  await downloadFile(
    `${API_BASE_URL}/api/reports/export/csv?${params.toString()}`,
    "securevault-audit-report.csv"
  );
};

export const exportAuditPdf = async (
  startDate,
  endDate
) => {
  const params = new URLSearchParams();

  if (startDate) {
    params.append("startDate", startDate);
  }

  if (endDate) {
    params.append("endDate", endDate);
  }

  await downloadFile(
    `${API_BASE_URL}/api/reports/export/pdf?${params.toString()}`,
    "securevault-audit-report.pdf"
  );
};