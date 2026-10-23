CREATE TABLE users (
    id UUID PRIMARY KEY,

    full_name VARCHAR(150) NOT NULL,

    email VARCHAR(320) NOT NULL,

    password_hash VARCHAR(255) NOT NULL,

    role VARCHAR(20) NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    institution_id UUID,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_user_role
        CHECK (role IN ('ADMIN', 'ISSUER', 'STUDENT')),

    CONSTRAINT chk_user_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED')),

    CONSTRAINT chk_issuer_institution
        CHECK (
            role <> 'ISSUER'
            OR institution_id IS NOT NULL
        ),

    CONSTRAINT fk_user_institution
        FOREIGN KEY (institution_id)
        REFERENCES institutions(id)
        ON DELETE RESTRICT
);

CREATE UNIQUE INDEX ux_users_email_lower
    ON users (LOWER(email));

CREATE INDEX ix_users_institution_id
    ON users (institution_id);