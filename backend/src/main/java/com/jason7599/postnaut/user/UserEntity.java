package com.jason7599.postnaut.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class UserEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(columnDefinition = "citext")
    private String username;

    private String displayName;

    private String passwordHash;

    private String friendCode;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    public UserEntity(String username, String displayName, String passwordHash, String friendCode) {
        this.username = username;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.friendCode = friendCode;
    }
}
