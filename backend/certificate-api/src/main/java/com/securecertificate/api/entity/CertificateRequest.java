package com.securecertificate.api.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificate_requests")
public class CertificateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_id")
    private User issuer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id")
    private Certificate certificate;

    @Column(name = "course_name", nullable = false, length = 200)
    private String courseName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CertificateRequestStatus status = CertificateRequestStatus.PENDING_ISSUER;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected CertificateRequest() {
        // Required by JPA
    }

    public CertificateRequest(
            User student,
            Institution institution,
            String courseName,
            String description) {

        this.student = student;
        this.institution = institution;
        this.courseName = courseName;
        this.description = description;
        this.status = CertificateRequestStatus.PENDING_ISSUER;
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

    public User getStudent() {
        return student;
    }

    public Institution getInstitution() {
        return institution;
    }

    public User getIssuer() {
        return issuer;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getDescription() {
        return description;
    }

    public CertificateRequestStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void acceptByIssuer(User issuer) {

        if (status != CertificateRequestStatus.PENDING_ISSUER) {

            throw new IllegalStateException(
                    "Request is not awaiting issuer review");
        }

        this.issuer = issuer;
        this.status = CertificateRequestStatus.PENDING_ADMIN;
    }

    public void reject(String reason) {

        if (reason == null ||
                reason.isBlank()) {

            throw new IllegalArgumentException(
                    "Rejection reason is required");
        }

        this.rejectionReason = reason.trim();

        this.status = CertificateRequestStatus.REJECTED;
    }

    public void complete(Certificate certificate) {

        this.certificate = certificate;

        this.status = CertificateRequestStatus.COMPLETED;
    }
}