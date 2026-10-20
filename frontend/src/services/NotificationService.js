const API_BASE_URL = "http://localhost:8080";

const getToken = () => {
  return localStorage.getItem("token");
};

const getHeaders = () => {
  const token = getToken();

  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
};

const handleResponse = async (response) => {
  let data = null;

  try {
    data = await response.json();
  } catch {
    data = null;
  }

  if (!response.ok) {
    const message =
      data?.message ||
      data ||
      `Request failed with status ${response.status}`;

    throw new Error(message);
  }

  return data;
};

// Get all notifications
export const getNotifications = async () => {
  const response = await fetch(`${API_BASE_URL}/api/notifications`, {
    method: "GET",
    headers: getHeaders(),
  });

  return handleResponse(response);
};

// Get unread notifications
export const getUnreadNotifications = async () => {
  const response = await fetch(
    `${API_BASE_URL}/api/notifications/unread`,
    {
      method: "GET",
      headers: getHeaders(),
    }
  );

  return handleResponse(response);
};

// Get unread notification count
export const getUnreadNotificationCount = async () => {
  const response = await fetch(
    `${API_BASE_URL}/api/notifications/unread-count`,
    {
      method: "GET",
      headers: getHeaders(),
    }
  );

  return handleResponse(response);
};

// Mark one notification as read
export const markNotificationAsRead = async (notificationId) => {
  const response = await fetch(
    `${API_BASE_URL}/api/notifications/${notificationId}/read`,
    {
      method: "PUT",
      headers: getHeaders(),
    }
  );

  return handleResponse(response);
};

// Mark all notifications as read
export const markAllNotificationsAsRead = async () => {
  const response = await fetch(
    `${API_BASE_URL}/api/notifications/read-all`,
    {
      method: "PUT",
      headers: getHeaders(),
    }
  );

  return handleResponse(response);
};

// Delete notification
export const deleteNotification = async (notificationId) => {
  const response = await fetch(
    `${API_BASE_URL}/api/notifications/${notificationId}`,
    {
      method: "DELETE",
      headers: getHeaders(),
    }
  );

  return handleResponse(response);
};