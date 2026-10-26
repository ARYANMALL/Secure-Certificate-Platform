CREATE TABLE certificate_requests (
    id UUID PRIMARY KEY,

    student_id UUID NOT NULL,
    institution_id UUID NOT NULL,
    issuer_id UUID,
    certificate_id UUID,

    course_name VARCHAR(200) NOT NULL,
    description TEXT,

    status VARCHAR(30) NOT NULL
        DEFAULT 'PENDING_ISSUER',

    rejection_reason VARCHAR(500),

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_request_student
        FOREIGN KEY (student_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_request_institution
        FOREIGN KEY (institution_id)
        REFERENCES institutions(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_request_issuer
        FOREIGN KEY (issuer_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_request_certificate
        FOREIGN KEY (certificate_id)
        REFERENCES certificates(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_certificate_request_status
        CHECK (
            status IN (
                'PENDING_ISSUER',
                'PENDING_ADMIN',
                'REJECTED',
                'COMPLETED'
            )
        )
);

CREATE INDEX ix_certificate_requests_student
    ON certificate_requests(student_id);

CREATE INDEX ix_certificate_requests_institution_status
    ON certificate_requests(institution_id, status);

CREATE INDEX ix_certificate_requests_issuer
    ON certificate_requests(issuer_id);