package com.bmtechnology.bmcrm.identity.application.command;

public record AuthenticateUserCommand(
        String email,
        String password
) {
}
