CREATE DATABASE IF NOT EXISTS ocms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ocms;

SET NAMES utf8mb4;

DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS behavior_record;
DROP TABLE IF EXISTS behavior_raw_data;
DROP TABLE IF EXISTS quiz_submission;
DROP TABLE IF EXISTS quiz_question;
DROP TABLE IF EXISTS quiz;
DROP TABLE IF EXISTS sign_in_penalty;
DROP TABLE IF EXISTS sign_in_record;
DROP TABLE IF EXISTS sign_in_task;
DROP TABLE IF EXISTS class_session;
DROP TABLE IF EXISTS course_enrollment;
DROP TABLE IF EXISTS course_audit_record;
DROP TABLE IF EXISTS course_content;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    real_name VARCHAR(100) NOT NULL,
    gender VARCHAR(16) DEFAULT NULL,
    phone VARCHAR(32) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    department_or_class VARCHAR(120) DEFAULT NULL,
    avatar VARCHAR(255) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_status (status),
    INDEX idx_user_real_name (real_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(32) NOT NULL UNIQUE,
    role_name VARCHAR(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_role_user (user_id),
    INDEX idx_user_role_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    perm_code VARCHAR(64) NOT NULL UNIQUE,
    perm_name VARCHAR(100) NOT NULL,
    description VARCHAR(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_name VARCHAR(100) NOT NULL,
    course_code VARCHAR(32) NOT NULL UNIQUE,
    course_intro TEXT,
    course_cover VARCHAR(255) DEFAULT NULL,
    content_summary TEXT,
    teacher_id BIGINT NOT NULL,
    audit_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    publish_status TINYINT NOT NULL DEFAULT 1,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    INDEX idx_course_teacher (teacher_id),
    INDEX idx_course_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE course_audit_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    auditor_id BIGINT NOT NULL,
    audit_status VARCHAR(20) NOT NULL,
    remark VARCHAR(255) DEFAULT NULL,
    audit_time DATETIME NOT NULL,
    INDEX idx_course_audit_course (course_id),
    INDEX idx_course_audit_time (audit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE course_content (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    content_title VARCHAR(150) NOT NULL,
    content_body TEXT NOT NULL,
    attachment_name VARCHAR(255) DEFAULT NULL,
    attachment_url VARCHAR(500) DEFAULT NULL,
    attachment_type VARCHAR(50) DEFAULT NULL,
    audit_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    auditor_id BIGINT DEFAULT NULL,
    audit_remark VARCHAR(255) DEFAULT NULL,
    publish_status TINYINT NOT NULL DEFAULT 1,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    INDEX idx_content_course (course_id),
    INDEX idx_content_teacher (teacher_id),
    INDEX idx_content_audit (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE course_enrollment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    enroll_time DATETIME NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    UNIQUE KEY uk_course_student (course_id, student_id),
    INDEX idx_enroll_student (student_id),
    INDEX idx_enroll_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE class_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    session_title VARCHAR(120) NOT NULL,
    session_start_time DATETIME NOT NULL,
    session_end_time DATETIME NOT NULL,
    session_status VARCHAR(20) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    INDEX idx_session_course (course_id),
    INDEX idx_session_teacher (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sign_in_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    task_title VARCHAR(120) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    task_status VARCHAR(20) NOT NULL,
    penalty_score INT NOT NULL DEFAULT 5,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    INDEX idx_sign_task_session (session_id),
    INDEX idx_sign_task_teacher (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sign_in_penalty (
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

CREATE TABLE sign_in_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    sign_in_time DATETIME NOT NULL,
    sign_in_status VARCHAR(20) NOT NULL,
    UNIQUE KEY uk_task_student (task_id, student_id),
    INDEX idx_sign_record_student (student_id),
    INDEX idx_sign_record_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE quiz (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    quiz_title VARCHAR(120) NOT NULL,
    quiz_desc TEXT,
    total_score INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    quiz_status VARCHAR(20) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    INDEX idx_quiz_session (session_id),
    INDEX idx_quiz_teacher (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE quiz_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    quiz_id BIGINT NOT NULL,
    question_title VARCHAR(255) NOT NULL,
    question_type VARCHAR(20) NOT NULL,
    options_json TEXT,
    correct_answer VARCHAR(255) NOT NULL,
    score INT NOT NULL,
    sort_no INT NOT NULL,
    INDEX idx_question_quiz (quiz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE quiz_submission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    quiz_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    answer_json TEXT,
    score INT NOT NULL,
    submit_status VARCHAR(20) NOT NULL,
    submit_time DATETIME NOT NULL,
    UNIQUE KEY uk_quiz_student (quiz_id, student_id),
    INDEX idx_submission_student (student_id),
    INDEX idx_submission_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE behavior_raw_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    class_session_id BIGINT NOT NULL,
    online_flag TINYINT NOT NULL,
    last_active_time DATETIME NOT NULL,
    heartbeat_time DATETIME NOT NULL,
    page_stay_duration_sec INT NOT NULL,
    focus_flag TINYINT NOT NULL,
    blur_duration_sec INT NOT NULL,
    mouse_click_count INT NOT NULL,
    key_input_count INT NOT NULL,
    sign_in_flag TINYINT NOT NULL,
    quiz_join_flag TINYINT NOT NULL,
    class_stay_duration_sec INT NOT NULL,
    base_activity_score INT NOT NULL,
    raw_data_json TEXT,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    INDEX idx_behavior_raw_student (student_id),
    INDEX idx_behavior_raw_session (class_session_id),
    INDEX idx_behavior_raw_time (heartbeat_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE behavior_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    class_session_id BIGINT NOT NULL,
    detect_time DATETIME NOT NULL,
    behavior_status VARCHAR(50) NOT NULL,
    behavior_type VARCHAR(50) NOT NULL,
    status_description VARCHAR(255) NOT NULL,
    activity_score INT NOT NULL,
    focus_flag TINYINT NOT NULL,
    exception_flag TINYINT NOT NULL,
    data_source VARCHAR(50) NOT NULL,
    raw_data_summary VARCHAR(255) DEFAULT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    INDEX idx_behavior_record_student (student_id),
    INDEX idx_behavior_record_teacher (teacher_id),
    INDEX idx_behavior_record_course (course_id),
    INDEX idx_behavior_record_session (class_session_id),
    INDEX idx_behavior_record_detect_time (detect_time),
    INDEX idx_behavior_record_exception (exception_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id BIGINT NOT NULL,
    operator_role VARCHAR(20) NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    operation_desc VARCHAR(255) NOT NULL,
    request_path VARCHAR(255) DEFAULT NULL,
    request_method VARCHAR(10) DEFAULT NULL,
    operation_time DATETIME NOT NULL,
    INDEX idx_operation_operator (operator_id),
    INDEX idx_operation_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
