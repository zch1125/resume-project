-- ============================================================
-- Resume Platform - 数据库初始化脚本
-- ============================================================

CREATE DATABASE IF NOT EXISTS resume_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE resume_platform;

-- 简历表
CREATE TABLE IF NOT EXISTS resume (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    session_id  VARCHAR(64)  NOT NULL COMMENT '前端生成的用户标识',
    file_name   VARCHAR(255) NOT NULL COMMENT '原始文件名',
    raw_text    LONGTEXT     NULL     COMMENT '提取的原始文本',
    parsed_json JSON         NULL     COMMENT 'AI解析的结构化信息',
    job_title   VARCHAR(100) NULL     COMMENT '目标岗位',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历表';

-- 修改建议表
CREATE TABLE IF NOT EXISTS suggestion (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    resume_id        BIGINT       NOT NULL COMMENT '关联简历ID',
    need_modify      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否需要修改',
    suggestions_json JSON         NULL     COMMENT '修改建议列表（JSON数组）',
    overall_comment  TEXT         NULL     COMMENT '整体评价',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_resume_id (resume_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历修改建议表';

-- 面试题目表
CREATE TABLE IF NOT EXISTS interview_question (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    resume_id        BIGINT       NOT NULL COMMENT '关联简历ID',
    job_title        VARCHAR(100) NOT NULL COMMENT '目标岗位',
    question         TEXT         NOT NULL COMMENT '面试题目',
    type             VARCHAR(20)  NOT NULL COMMENT '题型：技术/项目/行为',
    reference_answer TEXT         NULL     COMMENT '参考答案（AI生成）',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_resume_id (resume_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试题目表';

-- 答题记录表
CREATE TABLE IF NOT EXISTS answer_record (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    question_id  BIGINT       NOT NULL COMMENT '关联题目ID',
    user_answer  TEXT         NULL     COMMENT '用户作答内容',
    score        INT(1)       NULL     COMMENT '分数 0~5',
    score_reason VARCHAR(255) NULL     COMMENT '评分理由',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题记录表';

-- 面试会话表
CREATE TABLE IF NOT EXISTS interview_session (
    id                   BIGINT       AUTO_INCREMENT PRIMARY KEY,
    resume_id            BIGINT       NOT NULL COMMENT '关联简历ID',
    job_description      TEXT         NOT NULL COMMENT '岗位招聘信息',
    conversation_history LONGTEXT     NULL     COMMENT '对话历史(JSON数组)',
    status               VARCHAR(20)  NOT NULL DEFAULT 'in_progress' COMMENT '会话状态: in_progress/completed',
    create_time          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_resume_id (resume_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试会话表';

-- 简历修改建议表 - 增加对话历史字段
ALTER TABLE suggestion ADD COLUMN IF NOT EXISTS conversation_history LONGTEXT NULL COMMENT '对话历史(JSON数组)' AFTER overall_comment;
