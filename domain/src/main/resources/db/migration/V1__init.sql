-- ============================================================
-- super-trading initial schema (spec Section 5.4)
-- Scheduled web-automation platform, multi-user data model.
-- ============================================================

-- ============ APP USERS (console users) ============
CREATE TABLE app_user (
    id               BIGSERIAL PRIMARY KEY,
    email            VARCHAR(255) NOT NULL UNIQUE,
    display_name     VARCHAR(255),
    external_idp_sub VARCHAR(255) UNIQUE,                  -- Cognito subject claim
    role             VARCHAR(20) NOT NULL DEFAULT 'USER',  -- ADMIN | USER
    enabled          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============ SITE (shared target catalog) ============
CREATE TABLE site (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    base_url        TEXT NOT NULL,
    login_url       TEXT NOT NULL,
    selectors_json  JSONB NOT NULL,   -- {"username":"#u","password":"#p","green":".btn-green","red":".btn-red","logout":"#out"}
    created_by      BIGINT REFERENCES app_user(id),       -- audit only
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (base_url)
);

-- ============ SITE CREDENTIAL (bot login, owned by a user) ============
CREATE TABLE site_credential (
    id              BIGSERIAL PRIMARY KEY,
    owner_user_id   BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    site_id         BIGINT NOT NULL REFERENCES site(id),
    label           VARCHAR(255),                -- e.g. "my work account"
    username        VARCHAR(255) NOT NULL,
    secret_ref      TEXT NOT NULL,               -- Secrets Manager ARN; NEVER the password
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (owner_user_id, site_id, username)
);

-- ============ SCHEDULE (the "every N" config) ============
CREATE TABLE schedule (
    id                       BIGSERIAL PRIMARY KEY,
    credential_id            BIGINT NOT NULL REFERENCES site_credential(id) ON DELETE CASCADE,
    interval_seconds         INT NOT NULL CHECK (interval_seconds > 0),
    action_strategy          VARCHAR(50) NOT NULL DEFAULT 'random',
    wait_before_logout_ms    INT NOT NULL DEFAULT 3000,
    enabled                  BOOLEAN NOT NULL DEFAULT TRUE,
    eventbridge_schedule_arn TEXT,               -- link to the AWS schedule
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============ EXECUTION (one bot run) ============
CREATE TABLE execution (
    id                BIGSERIAL PRIMARY KEY,
    schedule_id       BIGINT NOT NULL REFERENCES schedule(id) ON DELETE CASCADE,
    started_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    ended_at          TIMESTAMPTZ,
    status            VARCHAR(20) NOT NULL DEFAULT 'RUNNING',  -- RUNNING | SUCCESS | FAILED
    error_message     TEXT,
    screenshot_s3_key TEXT,                       -- populated on failure
    idempotency_key   VARCHAR(100) UNIQUE         -- prevents double-recording on SQS retry
);

-- ============ ACTION (the green/red clicks) ============
CREATE TABLE action (
    id            BIGSERIAL PRIMARY KEY,
    execution_id  BIGINT NOT NULL REFERENCES execution(id) ON DELETE CASCADE,
    action_time   TIMESTAMPTZ NOT NULL DEFAULT now(),
    choice        VARCHAR(10) NOT NULL CHECK (choice IN ('GREEN','RED')),
    page_url      TEXT,
    duration_ms   INT
);

-- ============ AUDIT (admin changes) ============
CREATE TABLE audit_log (
    id            BIGSERIAL PRIMARY KEY,
    actor_user_id BIGINT REFERENCES app_user(id),
    entity_type   VARCHAR(50) NOT NULL,
    entity_id     BIGINT,
    action        VARCHAR(50) NOT NULL,           -- CREATE | UPDATE | DELETE | ENABLE | DISABLE
    details_json  JSONB,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============ INDEXES ============
CREATE INDEX idx_cred_owner       ON site_credential(owner_user_id);
CREATE INDEX idx_cred_site        ON site_credential(site_id);
CREATE INDEX idx_schedule_cred    ON schedule(credential_id);
CREATE INDEX idx_execution_sched  ON execution(schedule_id, started_at DESC);
CREATE INDEX idx_action_exec      ON action(execution_id);
CREATE INDEX idx_action_choice    ON action(choice, action_time);  -- green vs red analytics
