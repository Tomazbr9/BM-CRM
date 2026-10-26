package com.bmtechnology.bmcrm.identity.application.command;

public record CreateUserCommand(
        String name,
        String email,
        String password
) {
}
