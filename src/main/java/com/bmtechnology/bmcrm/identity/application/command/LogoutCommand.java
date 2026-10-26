package com.bmtechnology.bmcrm.identity.application.command;

public record LogoutCommand(
        String refreshToken
) {
}
