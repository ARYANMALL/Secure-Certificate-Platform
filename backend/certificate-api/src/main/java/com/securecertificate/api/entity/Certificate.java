package com.securecertificate.api.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "certificate_number", nullable = false, unique = true, length = 50)
    private String certificateNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issuer_id", nullable = false)
    private User issuer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    /*
     * Historical snapshots.
     */
    @Column(name = "student_name", nullable = false, length = 150)
    private String studentName;

    @Column(name = "issuer_name", nullable = false, length = 150)
    private String issuerName;

    @Column(name = "institution_name", nullable = false, length = 200)
    private String institutionName;

    @Column(name = "course_name", nullable = false, length = 200)
    private String courseName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CertificateStatus status = CertificateStatus.ACTIVE;

    @Column(name = "certificate_hash", nullable = false, length = 64)
    private String certificateHash;

    @Column(name = "digital_signature", nullable = false, columnDefinition = "TEXT")
    private String digitalSignature;

    @Column(name = "signature_algorithm", nullable = false, length = 50)
    private String signatureAlgorithm;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "revocation_reason", length = 500)
    private String revocationReason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Certificate() {
        // Required by JPA
    }

    public Certificate(
            String certificateNumber,
            User student,
            User issuer,
            Institution institution,
            String studentName,
            String issuerName,
            String institutionName,
            String courseName,
            String description,
            LocalDate issueDate,
            String certificateHash,
            String digitalSignature,
            String signatureAlgorithm) {

        this.certificateNumber = certificateNumber;
        this.student = student;
        this.issuer = issuer;
        this.institution = institution;
        this.studentName = studentName;
        this.issuerName = issuerName;
        this.institutionName = institutionName;
        this.courseName = courseName;
        this.description = description;
        this.issueDate = issueDate;
        this.certificateHash = certificateHash;
        this.digitalSignature = digitalSignature;
        this.signatureAlgorithm = signatureAlgorithm;
        this.status = CertificateStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {

        OffsetDateTime now = OffsetDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public User getStudent() {
        return student;
    }

    public User getIssuer() {
        return issuer;
    }

    public Institution getInstitution() {
        return institution;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public CertificateStatus getStatus() {
        return status;
    }

    public String getCertificateHash() {
        return certificateHash;
    }

    public String getDigitalSignature() {
        return digitalSignature;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public OffsetDateTime getRevokedAt() {
        return revokedAt;
    }

    public String getRevocationReason() {
        return revocationReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void revoke(String reason) {

        if (this.status == CertificateStatus.REVOKED) {
            throw new IllegalStateException(
                    "Certificate is already revoked");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Revocation reason is required");
        }

        this.status = CertificateStatus.REVOKED;

        this.revokedAt = OffsetDateTime.now();

        this.revocationReason = reason.trim();
    }
}