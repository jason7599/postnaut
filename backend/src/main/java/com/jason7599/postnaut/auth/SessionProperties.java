package com.jason7599.postnaut.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.session")
record SessionProperties(
        Duration slidingTtl,
        boolean cookieSecure
) {
}
