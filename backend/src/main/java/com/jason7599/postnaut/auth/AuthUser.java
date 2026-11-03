package com.jason7599.postnaut.auth;

import java.security.Principal;
import java.util.UUID;

public record AuthUser(
        UUID userId
) implements Principal {

    @Override
    public String getName() {
        return userId.toString();
    }
}
