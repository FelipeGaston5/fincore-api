package com.fincore.fincore_api.modules.auth.service;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fincore.fincore_api.modules.auth.dto.LoginRequest;
import com.fincore.fincore_api.modules.auth.dto.LoginResponse;
import com.fincore.fincore_api.security.jwt.JwtService;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        String normalizedEmail = request.email()
            .trim()
            .toLowerCase(Locale.ROOT);

        try {
            Authentication authentication =
                authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                        normalizedEmail,
                        request.password()
                    )
                );

            String accessToken =
                jwtService.generateAccessToken(authentication);

            return new LoginResponse(
                "Bearer",
                accessToken,
                jwtService.getAccessTokenTtlSeconds(),
                authentication.getName()
            );

        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "E-mail ou senha inválidos."
            );
        }
    }
}
