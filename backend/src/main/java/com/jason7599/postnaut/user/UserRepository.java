package com.jason7599.postnaut.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByUsername(String username);

    boolean existsByFriendCode(String friendCode);

    /** Pessimistic lock on the user row. NO KEY UPDATE means this won't block other FK calls */
    @Query(value = """
        SELECT id
        FROM users
        WHERE id = :id
        FOR NO KEY UPDATE
    """, nativeQuery = true)
    Optional<UUID> lockById(UUID id);
}
