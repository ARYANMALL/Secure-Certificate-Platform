package com.securecertificate.api.repository;

import com.securecertificate.api.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository
                extends JpaRepository<Certificate, UUID> {

        Optional<Certificate> findByCertificateNumber(
                        String certificateNumber);

        boolean existsByCertificateNumber(
                        String certificateNumber);

        @Query(value = "SELECT nextval('certificate_number_seq')", nativeQuery = true)
        long nextCertificateSequence();

        List<Certificate> findByStudent_IdOrderByIssueDateDesc(
                        UUID studentId);

        Optional<Certificate> findByIdAndStudent_Id(
                        UUID certificateId,
                        UUID studentId);
}