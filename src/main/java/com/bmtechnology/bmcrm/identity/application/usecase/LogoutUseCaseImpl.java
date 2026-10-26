package com.bmtechnology.bmcrm.identity.application.usecase;

import com.bmtechnology.bmcrm.identity.application.command.LogoutCommand;
import com.bmtechnology.bmcrm.identity.application.exception.InvalidRefreshTokenException;
import com.bmtechnology.bmcrm.identity.application.port.in.LogoutUseCase;
import com.bmtechnology.bmcrm.identity.application.port.out.RefreshTokenRepository;
import com.bmtechnology.bmcrm.identity.application.port.out.TokenHasher;
import com.bmtechnology.bmcrm.identity.domain.entity.RefreshTokenEntity;
import org.springframework.stereotype.Service;

@Service
public class LogoutUseCaseImpl implements LogoutUseCase {

    private final TokenHasher tokenHasher;
    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutUseCaseImpl(
            TokenHasher tokenHasher,
            RefreshTokenRepository refreshTokenRepository
    ){
        this.tokenHasher = tokenHasher;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void execute(LogoutCommand command) {

        String refreshToken = tokenHasher.hash(command.refreshToken());

        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByTokenHash(refreshToken)
                .orElseThrow(InvalidRefreshTokenException::new);

        refreshTokenEntity.revoke();

        refreshTokenRepository.save(refreshTokenEntity);

    }
}
