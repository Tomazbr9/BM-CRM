package com.bmtechnology.bmcrm.identity.application.usecase;

import com.bmtechnology.bmcrm.identity.application.command.AuthenticateUserCommand;
import com.bmtechnology.bmcrm.identity.application.dto.TokenResult;
import com.bmtechnology.bmcrm.identity.application.dto.AuthenticatedUser;
import com.bmtechnology.bmcrm.identity.application.port.in.AuthenticateUserUseCase;
import com.bmtechnology.bmcrm.identity.application.port.out.*;
import com.bmtechnology.bmcrm.identity.domain.entity.RefreshTokenEntity;
import com.bmtechnology.bmcrm.identity.domain.valueobjects.Email;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthenticateUserUseCaseImpl implements AuthenticateUserUseCase {

    private final UserAuthentication userAuthentication;
    private final TokenProvider tokenProvider;

    private final RefreshTokenGenerator refreshTokenGenerator;
    private final TokenHasher tokenHasher;
    private final RefreshTokenExpirationProvider refreshTokenExpirationProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthenticateUserUseCaseImpl(
            UserAuthentication userAuthentication,
            TokenProvider tokenprovider,
            RefreshTokenGenerator refreshTokenGenerator,
            TokenHasher tokenHasher,
            RefreshTokenExpirationProvider refreshTokenExpirationProvider,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.userAuthentication = userAuthentication;
        this.tokenProvider = tokenprovider;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.tokenHasher = tokenHasher;
        this.refreshTokenExpirationProvider = refreshTokenExpirationProvider;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public TokenResult execute(AuthenticateUserCommand command) {

        Email email = Email.of(command.email());

        AuthenticatedUser user =
                userAuthentication.authenticate(email.value(), command.password());

        String accessToken =
                tokenProvider.generateAccessToken(
                        user.email().value(),
                        user.role()
                );

        String refreshToken = refreshTokenGenerator.generate();
        String refreshTokenHash = tokenHasher.hash(refreshToken);
        LocalDateTime expiresAt = refreshTokenExpirationProvider.expiresAt();

        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.create(
                user.id(),
                refreshTokenHash,
                expiresAt
        );

        refreshTokenRepository.save(refreshTokenEntity);

        return new TokenResult(
                accessToken,
                refreshToken
        );
    }
}