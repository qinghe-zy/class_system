USE ocms;

CREATE TABLE IF NOT EXISTS course_content (
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

ALTER TABLE course_content ADD COLUMN IF NOT EXISTS attachment_name VARCHAR(255) DEFAULT NULL;
ALTER TABLE course_content ADD COLUMN IF NOT EXISTS attachment_url VARCHAR(500) DEFAULT NULL;
ALTER TABLE course_content ADD COLUMN IF NOT EXISTS attachment_type VARCHAR(50) DEFAULT NULL;

ALTER TABLE sign_in_task ADD COLUMN IF NOT EXISTS penalty_score INT NOT NULL DEFAULT 5;

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

INSERT INTO course_content(course_id, teacher_id, content_title, content_body, attachment_name, attachment_url, attachment_type, audit_status, auditor_id, audit_remark, publish_status, created_time, updated_time, deleted_flag)
SELECT 1,2,'第1讲课件与代码示例','包含Spring Boot快速入门课件、示例工程链接与课堂练习说明。','springboot-intro.pptx','/uploads/demo/springboot-intro.pptx','pptx','APPROVED',1,'内容完整，审核通过',1,NOW(),NOW(),0
WHERE NOT EXISTS (SELECT 1 FROM course_content WHERE course_id=1 AND content_title='第1讲课件与代码示例');

INSERT INTO course_content(course_id, teacher_id, content_title, content_body, attachment_name, attachment_url, attachment_type, audit_status, auditor_id, audit_remark, publish_status, created_time, updated_time, deleted_flag)
SELECT 2,2,'第2讲索引优化案例','包含执行计划案例、索引优化步骤与作业要求。','mysql-index.docx','/uploads/demo/mysql-index.docx','docx','PENDING',NULL,NULL,1,NOW(),NOW(),0
WHERE NOT EXISTS (SELECT 1 FROM course_content WHERE course_id=2 AND content_title='第2讲索引优化案例');

SELECT COUNT(*) AS content_count FROM course_content;
SELECT COUNT(*) AS penalty_table_ok FROM sign_in_penalty;
