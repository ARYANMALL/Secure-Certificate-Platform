CREATE SEQUENCE certificate_number_seq
    START WITH 1
    INCREMENT BY 1
    NO CYCLE;


CREATE TABLE certificates (

    id UUID PRIMARY KEY,

    certificate_number VARCHAR(50) NOT NULL UNIQUE,

    student_id UUID NOT NULL,

    issuer_id UUID NOT NULL,

    institution_id UUID NOT NULL,

    /*
     * These are snapshots of the names at issuance time.
     * This protects the historical certificate if a user
     * or institution later changes their name.
     */
    student_name VARCHAR(150) NOT NULL,

    issuer_name VARCHAR(150) NOT NULL,

    institution_name VARCHAR(200) NOT NULL,

    course_name VARCHAR(200) NOT NULL,

    description TEXT,

    issue_date DATE NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    /*
     * SHA-256 produces 32 bytes = 64 hexadecimal characters.
     */
    certificate_hash CHAR(64) NOT NULL,

    digital_signature TEXT NOT NULL,

    signature_algorithm VARCHAR(50) NOT NULL,

    revoked_at TIMESTAMPTZ,

    revocation_reason VARCHAR(500),

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_certificate_student
        FOREIGN KEY (student_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_certificate_issuer
        FOREIGN KEY (issuer_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_certificate_institution
        FOREIGN KEY (institution_id)
        REFERENCES institutions(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_certificate_status
        CHECK (
            status IN ('ACTIVE', 'REVOKED')
        ),

    CONSTRAINT chk_certificate_revocation
        CHECK (
            (
                status = 'ACTIVE'
                AND revoked_at IS NULL
                AND revocation_reason IS NULL
            )
            OR
            (
                status = 'REVOKED'
                AND revoked_at IS NOT NULL
                AND revocation_reason IS NOT NULL
            )
        )
);


CREATE INDEX ix_certificates_student_id
    ON certificates(student_id);


CREATE INDEX ix_certificates_issuer_id
    ON certificates(issuer_id);


CREATE INDEX ix_certificates_institution_id
    ON certificates(institution_id);