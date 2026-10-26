package com.bmtechnology.bmcrm.identity.presentation.controller;

import com.bmtechnology.bmcrm.identity.application.command.AuthenticateUserCommand;
import com.bmtechnology.bmcrm.identity.application.command.LogoutCommand;
import com.bmtechnology.bmcrm.identity.application.command.RefreshTokenCommand;
import com.bmtechnology.bmcrm.identity.application.dto.TokenResult;
import com.bmtechnology.bmcrm.identity.application.port.in.AuthenticateUserUseCase;
import com.bmtechnology.bmcrm.identity.application.port.in.LogoutUseCase;
import com.bmtechnology.bmcrm.identity.application.port.in.RefreshTokenUseCase;
import com.bmtechnology.bmcrm.identity.presentation.request.LoginRequest;
import com.bmtechnology.bmcrm.identity.presentation.request.RefreshTokenRequest;
import com.bmtechnology.bmcrm.identity.presentation.response.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest request){

        AuthenticateUserCommand command = new AuthenticateUserCommand(
                request.email(),
                request.password()
        );

        TokenResult result = authenticateUserUseCase.execute(command);

        LoginResponse response = new LoginResponse(
                result.accessToken(),
                result.refreshToken()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResult> refresh(@Valid @RequestBody RefreshTokenRequest request){

        RefreshTokenCommand command = new RefreshTokenCommand(request.refreshToken());

        TokenResult result = refreshTokenUseCase.execute(command);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request){

        LogoutCommand command = new LogoutCommand(request.refreshToken());

        logoutUseCase.execute(command);

        return ResponseEntity.noContent().build();

    }

}
