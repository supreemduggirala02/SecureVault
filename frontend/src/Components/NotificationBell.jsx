import { useEffect, useRef, useState } from "react";

import NotificationPanel from "./NotificationPanel";

import {
  getUnreadNotificationCount,
  getUnreadNotifications,
  markAllNotificationsAsRead,
  markNotificationAsRead,
  deleteNotification,
} from "../services/NotificationService";

function NotificationBell() {
  const [isOpen, setIsOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(null);
  const [error, setError] = useState("");

  const notificationRef = useRef(null);

  const loadUnreadData = async (showLoader = false) => {
    const token = localStorage.getItem("token");

    if (!token) {
      setNotifications([]);
      setUnreadCount(0);
      return;
    }

    if (showLoader) {
      setLoading(true);
    }

    try {
      const [countResponse, notificationsResponse] =
        await Promise.all([
          getUnreadNotificationCount(),
          getUnreadNotifications(),
        ]);

      setUnreadCount(
        Number(countResponse?.count || 0)
      );

      setNotifications(
        Array.isArray(notificationsResponse)
          ? notificationsResponse
          : []
      );

      setError("");
    } catch (err) {
      console.error(
        "Failed to load notifications:",
        err
      );

      setError(
        err.message ||
          "Unable to load notifications"
      );
    } finally {
      if (showLoader) {
        setLoading(false);
      }
    }
  };

  useEffect(() => {
    loadUnreadData();

    const interval = setInterval(() => {
      loadUnreadData();
    }, 30000);

    return () => {
      clearInterval(interval);
    };
  }, []);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        notificationRef.current &&
        !notificationRef.current.contains(
          event.target
        )
      ) {
        setIsOpen(false);
      }
    };

    document.addEventListener(
      "mousedown",
      handleClickOutside
    );

    return () => {
      document.removeEventListener(
        "mousedown",
        handleClickOutside
      );
    };
  }, []);

  const handleToggle = async () => {
    const nextState = !isOpen;

    setIsOpen(nextState);

    if (nextState) {
      await loadUnreadData(true);
    }
  };

  const handleMarkAsRead = async (notificationId) => {
    setActionLoading(notificationId);
    setError("");

    try {
      await markNotificationAsRead(
        notificationId
      );

      setNotifications((current) =>
        current.filter(
          (notification) =>
            notification.id !== notificationId
        )
      );

      setUnreadCount((current) =>
        Math.max(0, current - 1)
      );
    } catch (err) {
      console.error(
        "Failed to mark notification as read:",
        err
      );

      setError(
        err.message ||
          "Unable to mark notification as read"
      );
    } finally {
      setActionLoading(null);
    }
  };

  const handleMarkAllAsRead = async () => {
    if (unreadCount === 0) {
      return;
    }

    setActionLoading("all");
    setError("");

    try {
      await markAllNotificationsAsRead();

      setNotifications([]);
      setUnreadCount(0);
    } catch (err) {
      console.error(
        "Failed to mark all notifications as read:",
        err
      );

      setError(
        err.message ||
          "Unable to mark all notifications as read"
      );
    } finally {
      setActionLoading(null);
    }
  };

  const handleDelete = async (notificationId) => {
    setActionLoading(
      `delete-${notificationId}`
    );

    setError("");

    try {
      await deleteNotification(
        notificationId
      );

      setNotifications((current) =>
        current.filter(
          (notification) =>
            notification.id !== notificationId
        )
      );

      
      setUnreadCount((current) =>
        Math.max(0, current - 1)
      );
    } catch (err) {
      console.error(
        "Failed to delete notification:",
        err
      );

      setError(
        err.message ||
          "Unable to delete notification"
      );
    } finally {
      setActionLoading(null);
    }
  };

  return (
    <div
      className="notification-wrapper"
      ref={notificationRef}
    >
      <button
        type="button"
        className={`notification-bell ${
          unreadCount > 0
            ? "has-unread"
            : ""
        }`}
        onClick={handleToggle}
        aria-label={`Notifications${
          unreadCount > 0
            ? `, ${unreadCount} unread`
            : ""
        }`}
        aria-expanded={isOpen}
      >
        <span
          className="notification-bell-icon"
          aria-hidden="true"
        >
          🔔
        </span>

        {unreadCount > 0 && (
          <span className="notification-badge">
            {unreadCount > 99
              ? "99+"
              : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <NotificationPanel
          notifications={notifications}
          unreadCount={unreadCount}
          loading={loading}
          error={error}
          actionLoading={actionLoading}
          onMarkAsRead={handleMarkAsRead}
          onMarkAllAsRead={
            handleMarkAllAsRead
          }
          onDelete={handleDelete}
        />
      )}
    </div>
  );
}

export default NotificationBell;