package com.jason7599.postnaut.auth;

import com.jason7599.postnaut.auth.dto.UserLoginRequest;
import com.jason7599.postnaut.auth.dto.UserRegisterRequest;
import com.jason7599.postnaut.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
class AuthService {

    private final UserService userService;
    private final SessionService sessionService;

    String register(UserRegisterRequest request) {
        UUID userId = userService.register(request);
        return sessionService.create(userId);
    }

    String login(UserLoginRequest request) {
        UUID userId = userService.authenticate(request);
        return sessionService.create(userId);
    }

    void logout(String token) {
        sessionService.delete(token);
    }
}
