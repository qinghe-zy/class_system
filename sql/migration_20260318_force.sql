USE ocms;
ALTER TABLE course_content ADD COLUMN attachment_name VARCHAR(255) DEFAULT NULL;
ALTER TABLE course_content ADD COLUMN attachment_url VARCHAR(500) DEFAULT NULL;
ALTER TABLE course_content ADD COLUMN attachment_type VARCHAR(50) DEFAULT NULL;
ALTER TABLE sign_in_task ADD COLUMN penalty_score INT NOT NULL DEFAULT 5;
CREATE TABLE IF NOT EXISTS sign_in_penalty (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    penalty_type VARCHAR(50) NOT NULL,
    penalty_score INT NOT NULL,
    penalty_reason VARCHAR(255) DEFAULT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_penalty_task_student (task_id, student_id),
    INDEX idx_penalty_student (student_id),
    INDEX idx_penalty_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
UPDATE sign_in_task SET penalty_score = 5 WHERE penalty_score IS NULL;
