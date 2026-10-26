package com.bmtechnology.bmcrm.identity.application.port.in;

import com.bmtechnology.bmcrm.identity.application.command.LogoutCommand;

public interface LogoutUseCase {

    void execute(LogoutCommand command);
}
