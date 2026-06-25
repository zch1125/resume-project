CREATE TABLE IF NOT EXISTS resume (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    session_id  VARCHAR(64)  NOT NULL,
    file_name   VARCHAR(255) NOT NULL,
    raw_text    CLOB         NULL,
    parsed_json TEXT         NULL,
    job_title   VARCHAR(100) NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_resume_session ON resume(session_id);

CREATE TABLE IF NOT EXISTS suggestion (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    resume_id        BIGINT       NOT NULL,
    need_modify      BOOLEAN      NOT NULL DEFAULT TRUE,
    suggestions_json TEXT         NULL,
    overall_comment  TEXT         NULL,
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_suggestion_resume ON suggestion(resume_id);

CREATE TABLE IF NOT EXISTS interview_question (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    resume_id        BIGINT       NOT NULL,
    job_title        VARCHAR(100) NOT NULL,
    question         TEXT         NOT NULL,
    type             VARCHAR(20)  NOT NULL,
    reference_answer TEXT         NULL,
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_question_resume ON interview_question(resume_id);

CREATE TABLE IF NOT EXISTS answer_record (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    question_id  BIGINT       NOT NULL,
    user_answer  TEXT         NULL,
    score        INT          NULL,
    score_reason VARCHAR(255) NULL,
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_answer_question ON answer_record(question_id);
