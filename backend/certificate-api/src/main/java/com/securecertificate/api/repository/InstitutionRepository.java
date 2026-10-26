package com.securecertificate.api.repository;

import com.securecertificate.api.entity.Institution;
import com.securecertificate.api.entity.InstitutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstitutionRepository
        extends JpaRepository<Institution, UUID> {

    Optional<Institution> findByRegistrationNumber(
            String registrationNumber);

    List<Institution> findByStatusOrderByNameAsc(
            InstitutionStatus status);
}