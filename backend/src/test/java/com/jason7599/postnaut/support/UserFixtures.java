package com.jason7599.postnaut.support;

import com.jason7599.postnaut.auth.UserRegisterRequest;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestComponent
@RequiredArgsConstructor
public class UserFixtures {

    public static final String PASSWORD = "123456";

    public record TestUser(UUID id, String username, Cookie session) {}

    private final TestClient client;
    private final JdbcTemplate jdbc;

    public TestUser create(String username) throws Exception {
        Cookie session = client.post(
                "/auth/register",
                new UserRegisterRequest(
                        username,
                        username,
                        PASSWORD
                ),
                null
        ).andExpect(status().isCreated())
        .andReturn().getResponse().getCookie("session");

        UUID id = jdbc.queryForObject(
                "SELECT id FROM users WHERE username = ?", UUID.class, username
        );

        return new TestUser(id, username, session);
    }
}
