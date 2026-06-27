-- ============================================================
-- Resume Platform - 鏁版嵁搴撳垵濮嬪寲鑴氭湰
-- ============================================================

CREATE DATABASE IF NOT EXISTS resume_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE resume_platform;

-- 绠€鍘嗚〃
CREATE TABLE IF NOT EXISTS resume (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    session_id  VARCHAR(64)  NOT NULL COMMENT '鍓嶇鐢熸垚鐢ㄦ埛鏍囪瘑',
    user_id     BIGINT       NULL     COMMENT '鍏宠仈鐢ㄦ埛ID',
    file_name   VARCHAR(255) NOT NULL COMMENT '鍘熷鏂囦欢鍚?,
    raw_text    LONGTEXT     NULL     COMMENT '鎻愬彇鐨勫師濮嬫枃鏈?,
    parsed_json JSON         NULL     COMMENT 'AI瑙ｆ瀽鐨勭粨鏋勫寲淇℃伅',
    job_title   VARCHAR(100) NULL     COMMENT '鐩爣宀椾綅',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绠€鍘嗚〃';

-- 淇敼寤鸿琛?CREATE TABLE IF NOT EXISTS suggestion (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    resume_id        BIGINT       NOT NULL COMMENT '鍏宠仈绠€鍘咺D',
    need_modify      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '鏄惁闇€瑕佷慨鏀?,
    suggestions_json JSON         NULL     COMMENT '淇敼寤鸿鍒楄〃锛圝SON鏁扮粍锛?,
    overall_comment  TEXT         NULL     COMMENT '鏁翠綋璇勪环',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_resume_id (resume_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绠€鍘嗕慨鏀瑰缓璁〃';

-- 闈㈣瘯棰樼洰琛?CREATE TABLE IF NOT EXISTS interview_question (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    resume_id        BIGINT       NOT NULL COMMENT '鍏宠仈绠€鍘咺D',
    job_title        VARCHAR(100) NOT NULL COMMENT '鐩爣宀椾綅',
    question         TEXT         NOT NULL COMMENT '闈㈣瘯棰樼洰',
    type             VARCHAR(20)  NOT NULL COMMENT '棰樺瀷锛氭妧鏈?椤圭洰/琛屼负',
    reference_answer TEXT         NULL     COMMENT '鍙傝€冪瓟妗堬紙AI鐢熸垚锛?,
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_resume_id (resume_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='闈㈣瘯棰樼洰琛?;

-- 绛旈璁板綍琛?CREATE TABLE IF NOT EXISTS answer_record (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    question_id  BIGINT       NOT NULL COMMENT '鍏宠仈棰樼洰ID',
    user_answer  TEXT         NULL     COMMENT '鐢ㄦ埛浣滅瓟鍐呭',
    score        INT(1)       NULL     COMMENT '鍒嗘暟 0~5',
    score_reason VARCHAR(255) NULL     COMMENT '璇勫垎鐞嗙敱',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绛旈璁板綍琛?;

-- 闈㈣瘯浼氳瘽琛?CREATE TABLE IF NOT EXISTS interview_session (
    id                   BIGINT       AUTO_INCREMENT PRIMARY KEY,
    resume_id            BIGINT       NOT NULL COMMENT '鍏宠仈绠€鍘咺D',
    job_description      TEXT         NOT NULL COMMENT '宀椾綅鎷涜仒淇℃伅',
    conversation_history LONGTEXT     NULL     COMMENT '瀵硅瘽鍘嗗彶(JSON鏁扮粍)',
    status               VARCHAR(20)  NOT NULL DEFAULT 'in_progress' COMMENT '浼氳瘽鐘舵€? in_progress/completed',
    create_time          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_resume_id (resume_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='闈㈣瘯浼氳瘽琛?;

-- 绠€鍘嗕慨鏀瑰缓璁〃 - 澧炲姞瀵硅瘽鍘嗗彶瀛楁
ALTER TABLE suggestion ADD COLUMN IF NOT EXISTS conversation_history LONGTEXT NULL COMMENT '瀵硅瘽鍘嗗彶(JSON鏁扮粍)' AFTER overall_comment;
-- 鐢ㄦ埛琛?CREATE TABLE IF NOT EXISTS resume_user (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '鐢ㄦ埛鍚?,
    password    VARCHAR(255) NOT NULL COMMENT '鍔犲瘑瀵嗙爜',
    nickname    VARCHAR(50)  NULL     COMMENT '鏄电О',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鐢ㄦ埛琛?;

