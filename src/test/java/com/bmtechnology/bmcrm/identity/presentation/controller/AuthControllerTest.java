package com.bmtechnology.bmcrm.identity.presentation.controller;

import com.bmtechnology.bmcrm.identity.application.dto.TokenResult;
import com.bmtechnology.bmcrm.identity.application.exception.ExpiredRefreshTokenException;
import com.bmtechnology.bmcrm.identity.application.exception.InvalidRefreshTokenException;
import com.bmtechnology.bmcrm.identity.application.exception.RevokedRefreshTokenException;
import com.bmtechnology.bmcrm.identity.application.port.in.AuthenticateUserUseCase;
import com.bmtechnology.bmcrm.identity.application.port.in.LogoutUseCase;
import com.bmtechnology.bmcrm.identity.application.port.in.RefreshTokenUseCase;
import com.bmtechnology.bmcrm.identity.infrastructure.security.JwtAuthenticationFilter;
import com.bmtechnology.bmcrm.identity.infrastructure.security.JwtService;
import com.bmtechnology.bmcrm.identity.presentation.exception.IdentityExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(IdentityExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthenticateUserUseCase authenticateUserUseCase;

    @MockitoBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockitoBean
    private LogoutUseCase logoutUseCase;

    @Test
    void shouldRefreshTokens() throws Exception {

        TokenResult result =
                new TokenResult(
                        "new-access-token",
                        "new-refresh-token"
                );

        when(refreshTokenUseCase.execute(any()))
                .thenReturn(result);

        String json = """
                {
                  "refreshToken": "valid-refresh-token"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken")
                        .value("new-refresh-token"));
    }

    @Test
    void shouldLogout() throws Exception {

        String json = """
            {
              "refreshToken": "valid-refresh-token"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/logout")
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnBadRequestWhenRefreshTokenIsBlank() throws Exception {

        String json = """
            {
              "refreshToken": ""
            }
            """;

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {

        when(authenticateUserUseCase.execute(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        String json = """
            {
              "email": "bruno@email.com",
              "password": "senha-errada"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value("E-mail ou senha inválidos"));
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshTokenIsInvalid() throws Exception {

        when(refreshTokenUseCase.execute(any()))
                .thenThrow(new InvalidRefreshTokenException());

        String json = """
            {
              "refreshToken": "invalid-refresh-token"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value("Refresh token inválido"));
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshTokenIsExpired() throws Exception {

        when(refreshTokenUseCase.execute(any()))
                .thenThrow(new ExpiredRefreshTokenException());

        String json = """
            {
              "refreshToken": "expired-refresh-token"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Refresh Token expirado"));
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshTokenIsRevoked() throws Exception {

        when(refreshTokenUseCase.execute(any()))
                .thenThrow(new RevokedRefreshTokenException());

        String json = """
            {
              "refreshToken": "revoked-refresh-token"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Refresh Token revogado"));
    }
}