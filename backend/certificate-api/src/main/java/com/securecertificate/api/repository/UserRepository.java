package com.securecertificate.api.repository;

import com.securecertificate.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.securecertificate.api.entity.UserRole;
import com.securecertificate.api.entity.UserStatus;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
    
   List<User> findByRoleAndStatusAndInstitution_IdOrderByFullNameAsc(
        UserRole role,
        UserStatus status,
        UUID institutionId
);
}