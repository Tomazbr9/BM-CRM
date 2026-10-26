package com.bmtechnology.bmcrm.identity.application.dto;

public record TokenResult(
        String accessToken,
        String refreshToken
) {}