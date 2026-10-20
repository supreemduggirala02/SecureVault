const formatTimeAgo = (dateValue) => {
  if (!dateValue) return "";

  const date = new Date(dateValue);

  if (Number.isNaN(date.getTime())) {
    return "";
  }

  const now = new Date();
  const diffSeconds = Math.floor((now - date) / 1000);

  if (diffSeconds < 10) return "Just now";
  if (diffSeconds < 60) return `${diffSeconds}s ago`;

  const diffMinutes = Math.floor(diffSeconds / 60);
  if (diffMinutes < 60) return `${diffMinutes}m ago`;

  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) return `${diffHours}h ago`;

  const diffDays = Math.floor(diffHours / 24);
  if (diffDays < 7) return `${diffDays}d ago`;

  return date.toLocaleDateString();
};

const getNotificationIcon = (type) => {
  switch (type) {
    case "LOGIN_SUCCESS":
      return "🔐";

    case "MFA_LOGIN_SUCCESS":
      return "🛡️";

    case "MFA_ENABLED":
      return "🔒";

    case "PASSWORD_RESET_REQUEST":
      return "🔑";

    case "PASSWORD_RESET_SUCCESS":
      return "✅";

    case "CREDENTIAL_SHARED":
      return "📤";

    case "CREDENTIAL_SHARE_REVOKED":
      return "🚫";

    default:
      return "🔔";
  }
};

const getNotificationTitle = (type) => {
  switch (type) {
    case "LOGIN_SUCCESS":
      return "Successful Login";

    case "MFA_LOGIN_SUCCESS":
      return "MFA Login Successful";

    case "MFA_ENABLED":
      return "MFA Enabled";

    case "PASSWORD_RESET_REQUEST":
      return "Password Reset Requested";

    case "PASSWORD_RESET_SUCCESS":
      return "Password Changed";

    case "CREDENTIAL_SHARED":
      return "Credential Shared";

    case "CREDENTIAL_SHARE_REVOKED":
      return "Credential Access Revoked";

    default:
      return "Security Notification";
  }
};

function NotificationPanel({
  notifications,
  unreadCount,
  loading,
  error,
  actionLoading,
  onMarkAsRead,
  onMarkAllAsRead,
  onDelete,
}) {
  return (
    <div className="notification-panel">
      {/* Header */}
      <div className="notification-panel-header">
        <div className="notification-panel-heading">
          <div className="notification-panel-title-row">
            <h3>Notifications</h3>

            {unreadCount > 0 && (
              <span className="notification-header-count">
                {unreadCount}
              </span>
            )}
          </div>

          <span className="notification-panel-subtitle">
            {unreadCount === 0
              ? "You're all caught up"
              : `${unreadCount} unread notification${
                  unreadCount === 1 ? "" : "s"
                }`}
          </span>
        </div>

        <button
          type="button"
          className="notification-mark-all"
          onClick={onMarkAllAsRead}
          disabled={
            unreadCount === 0 ||
            actionLoading === "all"
          }
        >
          {actionLoading === "all"
            ? "Marking..."
            : "Mark all read"}
        </button>
      </div>

      {/* Error */}
      {error && (
        <div className="notification-error">
          <span className="notification-error-icon">!</span>
          <span>{error}</span>
        </div>
      )}

      {/* Notification list */}
      <div className="notification-list">
        {loading ? (
          <div className="notification-loading">
            <div className="notification-spinner"></div>
            <span>Loading notifications...</span>
          </div>
        ) : notifications.length === 0 ? (
          <div className="notification-empty">
            <div className="notification-empty-icon">
              🔔
            </div>

            <h4>No new notifications</h4>

            <p>
              Security alerts and account activity will
              appear here.
            </p>
          </div>
        ) : (
          notifications.map((notification) => {
            const deleteLoading =
              actionLoading ===
              `delete-${notification.id}`;

            const readLoading =
              actionLoading === notification.id;

            return (
              <div
                key={notification.id}
                className={`notification-item ${
                  notification.read ? "read" : "unread"
                }`}
              >
                {/* Icon */}
                <div className="notification-item-icon">
                  {getNotificationIcon(notification.type)}
                </div>

                {/* Content */}
                <div className="notification-item-content">
                  <div className="notification-item-top">
                    <div className="notification-title-wrapper">
                      <h4>
                        {getNotificationTitle(
                          notification.type
                        )}
                      </h4>

                      {!notification.read && (
                        <span className="notification-unread-dot"></span>
                      )}
                    </div>

                    <span className="notification-time">
                      {formatTimeAgo(
                        notification.createdAt
                      )}
                    </span>
                  </div>

                  <p className="notification-message">
                    {notification.message}
                  </p>

                  {/* Actions */}
                  <div className="notification-item-actions">
                    {!notification.read && (
                      <button
                        type="button"
                        onClick={() =>
                          onMarkAsRead(notification.id)
                        }
                        disabled={readLoading}
                      >
                        {readLoading
                          ? "..."
                          : "Mark as read"}
                      </button>
                    )}

                    <button
                      type="button"
                      className="notification-delete"
                      onClick={() =>
                        onDelete(notification.id)
                      }
                      disabled={deleteLoading}
                    >
                      {deleteLoading
                        ? "..."
                        : "Delete"}
                    </button>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Footer */}
      <div className="notification-panel-footer">
        <span className="notification-footer-status">
          <span className="notification-status-dot"></span>
          SecureVault Security Center
        </span>
      </div>
    </div>
  );
}

export default NotificationPanel;