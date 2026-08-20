CREATE TABLE IF NOT EXISTS application_status_history
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT      NOT NULL,
    user_id        BIGINT      NOT NULL,
    from_status    VARCHAR(32) NULL,
    to_status      VARCHAR(32) NOT NULL,
    changed_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_history_application (application_id, changed_at),
    INDEX idx_history_user_status (user_id, to_status, application_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO application_status_history (application_id, user_id, from_status, to_status)
SELECT a.id, a.user_id, NULL, a.status
FROM job_application a
WHERE a.is_delete = 0
  AND NOT EXISTS (
      SELECT 1 FROM application_status_history h WHERE h.application_id = a.id
  );
