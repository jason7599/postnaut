package com.jason7599.postnaut;

import com.jason7599.postnaut.error.ApiErrorCode;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest         // starts the full spring application
@AutoConfigureMockMvc   // mockmvc to send fake HTTP requests without opening a real port
public abstract class IntegrationTest {

    @ServiceConnection  // Points my app's database config at this container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @SuppressWarnings("resource")
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7").withExposedPorts(6379);

    static {
        postgres.start();
        redis.start();
    }

    @Autowired protected MockMvc mvc;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected StringRedisTemplate redisTemplate;
    @Autowired protected ObjectMapper objectMapper;

    @BeforeEach
    protected void clear() {
        jdbc.execute("TRUNCATE TABLE users CASCADE");

        redisTemplate.execute((RedisCallback<Void>) conn -> {
            conn.serverCommands().flushAll();
            return null;
        });
    }

    protected static ResultMatcher apiError(ApiErrorCode code) {
        return result -> {
            status().is(code.getStatus().value()).match(result);
            jsonPath("$.code").value(code.name()).match(result);
        };
    }

    protected ResultActions post(String path, Object body, Cookie cookie) throws Exception {
        var request = MockMvcRequestBuilders.post(path);
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body));
        }

        if (cookie != null) request.cookie(cookie);

        return mvc.perform(request);
    }

    protected ResultActions get(String path, Cookie cookie) throws Exception {
        var request = MockMvcRequestBuilders.get(path);

        if (cookie != null) request.cookie(cookie);

        return mvc.perform(request);
    }
}
