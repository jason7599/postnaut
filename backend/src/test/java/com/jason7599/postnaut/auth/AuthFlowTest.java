package com.jason7599.postnaut.auth;

import com.jason7599.postnaut.IntegrationTest;
import com.jason7599.postnaut.auth.dto.UserLoginRequest;
import com.jason7599.postnaut.auth.dto.UserRegisterRequest;
import com.jason7599.postnaut.error.ApiErrorCode;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class AuthFlowTest extends IntegrationTest {

    @Autowired PasswordEncoder passwordEncoder;
    @Autowired SessionService sessionService;

    @Test
    void register_validRequest_createsUserAndSession() throws Exception {
        Cookie session = register("jason", "jux", "123456")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getCookie(SessionCookieService.COOKIE_NAME);

        assertThat(session).isNotNull();

        var user = jdbc.queryForMap(
                "SELECT * FROM users WHERE username = ?", "jason");

        assertThat(sessionService.resolve(session.getValue()))
                .contains((UUID) user.get("id"));

        assertThat(user.get("display_name")).isEqualTo("jux");
        assertThat(user.get("friend_code")).isNotNull();
        assertThat(passwordEncoder.matches(
                "123456", (String) user.get("password_hash"))).isTrue();
    }

    @Test
    void register_usernameTaken_fails() throws Exception {
        register("jason", "jux", "123456")
                .andExpect(status().isCreated());

        register("jason", "jjj", "123456")
                .andExpect(apiError(ApiErrorCode.USERNAME_TAKEN));
    }

    @ParameterizedTest
    @CsvSource({
            "ja,    jux,    123456",
            "jason, '',     123456",
            "jason, jux,    1234"
    })
    void register_invalidFields_fails(String username, String displayName, String password) throws Exception {
        register(username, displayName, password)
                .andExpect(apiError(ApiErrorCode.VALIDATION_ERROR));

        assertThat(
                jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class))
                .isZero();
    }

    @Test
    void login_validRequest_createsUserAndSession() throws Exception {
        register("jason", "jux", "123456");

        Cookie session = login("jason", "123456")
                .andExpect(status().isNoContent())
                .andReturn().getResponse().getCookie(SessionCookieService.COOKIE_NAME);

        assertThat(session).isNotNull();

        UUID userId = jdbc.queryForObject(
                "SELECT id FROM users WHERE username = ?", UUID.class, "jason");

        assertThat(sessionService.resolve(session.getValue())).contains(userId);
    }

    @Test
    void login_badCredentials_fails() throws Exception {
        register("jason", "jux", "123456");

        login("jason", "123457")
                .andExpect(apiError(ApiErrorCode.BAD_CREDENTIALS));

        login("jasonsanus", "123456")
                .andExpect(apiError(ApiErrorCode.BAD_CREDENTIALS));
    }

    @Test
    void logout_validSession_clearsRedis() throws Exception {
        Cookie session = register("jason", "jux", "123456")
                .andReturn().getResponse().getCookie(SessionCookieService.COOKIE_NAME);

        assertThat(session).isNotNull();

        UUID userId = jdbc.queryForObject(
                "SELECT id FROM users WHERE username = ?", UUID.class, "jason");

        assertThat(sessionService.resolve(session.getValue())).contains(userId);

        logout(session)
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(SessionCookieService.COOKIE_NAME, 0));

        assertThat(sessionService.resolve(session.getValue())).isEmpty();
    }

    private ResultActions register(String username, String displayName, String password) throws Exception {
        return post(
                "/auth/register",
                new UserRegisterRequest(
                        username,
                        displayName,
                        password
                ),
                null
        );
    }

    private ResultActions login(String username, String password) throws Exception {
        return post(
                "/auth/login",
                new UserLoginRequest(
                        username,
                        password
                ),
                null
        );
    }

    private ResultActions logout(Cookie cookie) throws Exception {
        return post(
                "/auth/logout",
                null,
                cookie
        );
    }
}
