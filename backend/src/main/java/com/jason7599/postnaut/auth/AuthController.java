package com.jason7599.postnaut.auth;

import com.jason7599.postnaut.auth.dto.UserLoginRequest;
import com.jason7599.postnaut.auth.dto.UserRegisterRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
class AuthController {

    private final AuthService authService;
    private final SessionCookieService cookieService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    void register(
            @RequestBody @Valid UserRegisterRequest request,
            HttpServletResponse response
    ) {
        cookieService.set(response, authService.register(request));
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void login(
            @RequestBody @Valid UserLoginRequest request,
            HttpServletResponse response
    ) {
        cookieService.set(response, authService.login(request));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(
            @CookieValue(name = SessionCookieService.COOKIE_NAME, required = false) String token,
            HttpServletResponse response
    ) {
        if (token != null) authService.logout(token);
        cookieService.clear(response);
    }
}
