package com.bmtechnology.bmcrm.identity.application.port.in;

import com.bmtechnology.bmcrm.identity.application.command.CreateUserCommand;

import java.util.UUID;

public interface CreateUserUseCase {

    UUID execute(CreateUserCommand command);
}
