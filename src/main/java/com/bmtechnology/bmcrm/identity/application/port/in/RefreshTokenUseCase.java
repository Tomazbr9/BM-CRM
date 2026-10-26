package com.bmtechnology.bmcrm.identity.application.port.in;

import com.bmtechnology.bmcrm.identity.application.command.RefreshTokenCommand;
import com.bmtechnology.bmcrm.identity.application.dto.TokenResult;

public interface RefreshTokenUseCase {
    TokenResult execute(RefreshTokenCommand command);
}
