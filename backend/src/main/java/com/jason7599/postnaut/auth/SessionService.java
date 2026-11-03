package com.jason7599.postnaut.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class SessionService {

    static final String SESSION_PREFIX = "session:";

    private final StringRedisTemplate redis;
    private final SessionTokenService tokenService;
    private final SessionProperties props;

    /** Generate random token, store hash -> userId, return raw token for cookie */
    String create(UUID userId) {
        String token = tokenService.create();
        redis.opsForValue().set(sessionKey(token), userId.toString(), props.slidingTtl());
        return token;
    }

    /** Find userId by token, reset the sliding TTL if present */
    Optional<UUID> resolve(String token) {
        // GETEX: read the value and reset TTL in one atomic command
        String val = redis.opsForValue().getAndExpire(sessionKey(token), props.slidingTtl());
        return Optional.ofNullable(val).map(UUID::fromString);
    }

    void delete(String token) {
        redis.delete(sessionKey(token));
    }

    private String sessionKey(String token) {
        return SESSION_PREFIX + tokenService.hash(token);
    }
}
