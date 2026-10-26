package com.bmtechnology.bmcrm.identity.application.port.in;

import com.bmtechnology.bmcrm.identity.application.command.AuthenticateUserCommand;
import com.bmtechnology.bmcrm.identity.application.dto.TokenResult;

public interface AuthenticateUserUseCase {

    TokenResult execute(AuthenticateUserCommand command);
}