package com.securecertificate.api.repository;

import com.securecertificate.api.entity.CertificateRequest;
import com.securecertificate.api.entity.CertificateRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CertificateRequestRepository
        extends JpaRepository<CertificateRequest, UUID> {

    /*
     * ---------------------------------------------------------
     * STUDENT REQUESTS
     * ---------------------------------------------------------
     *
     * Returns only requests belonging to one student.
     */
    List<CertificateRequest> findByStudent_IdOrderByCreatedAtDesc(
            UUID studentId);

    /*
     * ---------------------------------------------------------
     * CENTRAL ISSUER REQUESTS
     * ---------------------------------------------------------
     *
     * IMPORTANT:
     *
     * We intentionally DO NOT filter by institution.
     *
     * Dr. Amit Verma is a central issuer and can
     * process requests from every institution.
     */
    List<CertificateRequest> findByStatusOrderByCreatedAtAsc(
            CertificateRequestStatus status);

}