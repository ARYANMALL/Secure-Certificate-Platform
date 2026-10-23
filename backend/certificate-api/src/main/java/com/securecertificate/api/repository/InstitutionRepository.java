package com.securecertificate.api.repository;

import com.securecertificate.api.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InstitutionRepository extends JpaRepository<Institution, UUID> {

    Optional<Institution> findByRegistrationNumber(String registrationNumber);

}