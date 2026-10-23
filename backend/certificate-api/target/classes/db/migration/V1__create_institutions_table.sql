CREATE TABLE institutions (
    id UUID PRIMARY KEY,

    name VARCHAR(200) NOT NULL,

    registration_number VARCHAR(100) NOT NULL UNIQUE,

    email VARCHAR(320),

    address TEXT,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_institution_status
        CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED'))
);