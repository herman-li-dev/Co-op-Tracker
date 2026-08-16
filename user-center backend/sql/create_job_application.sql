CREATE TABLE IF NOT EXISTS job_application
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT                              NOT NULL,
    company_name        VARCHAR(128)                        NOT NULL,
    job_title           VARCHAR(128)                        NOT NULL,
    location            VARCHAR(128)                        NULL,
    job_url             VARCHAR(1024)                       NULL,
    status              VARCHAR(32) DEFAULT 'SAVED'         NOT NULL,
    work_mode           VARCHAR(32)                         NULL,
    applied_date        DATE                                NULL,
    deadline            DATE                                NULL,
    next_follow_up_date DATE                                NULL,
    next_step           VARCHAR(255)                        NULL,
    notes               TEXT                                NULL,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP  NOT NULL,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP  NOT NULL
        ON UPDATE CURRENT_TIMESTAMP,
    is_delete           TINYINT DEFAULT 0                   NOT NULL,

    INDEX idx_user_status (user_id, status),
    INDEX idx_user_follow_up (user_id, next_follow_up_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
