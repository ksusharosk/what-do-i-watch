package com.whatiwatch.domain.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data-access interface for users. Spring Data JPA generates the implenetation
 * at runtime.
 */
public interface UserRepository extends JpaRepository<UserEntity, String> {
    
    /**
     * Finds a user by their Google ID. Spring generates the query fro the
     * method name - "findBy" + "GoogleId"
     */
    Optional<UserEntity> findByGoogleId(String googleId);

    // Finds a user by email
    Optional<UserEntity> findByEmail(String email);

    // True if a user with this Google ID exists
    boolean existsByGoogleId(String googleId);

}
