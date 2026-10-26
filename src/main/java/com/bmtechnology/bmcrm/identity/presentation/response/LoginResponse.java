package com.bmtechnology.bmcrm.identity.presentation.response;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
