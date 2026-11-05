package com.jason7599.postnaut.support;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@TestComponent
@RequiredArgsConstructor
public class TestClient {

    private final MockMvc mvc;
    private final ObjectMapper objectMapper;

    public ResultActions post(String path, Object body, Cookie cookie) throws Exception {
        var request = MockMvcRequestBuilders.post(path);

        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body));
        }
        if (cookie != null) request.cookie(cookie);

        return mvc.perform(request);
    }

    public ResultActions get(String path, Cookie cookie) throws Exception {
        var request = MockMvcRequestBuilders.get(path);

        if (cookie != null) request.cookie(cookie);

        return mvc.perform(request);
    }

    public <T> T read(ResultActions result, TypeReference<T> type) throws Exception {
        String json = result.andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readValue(json, type);
    }
}
