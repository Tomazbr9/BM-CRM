package com.bmtechnology.bmcrm.identity.application.dto;

import com.bmtechnology.bmcrm.identity.domain.valueobjects.Email;

import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        Email email,
        String role
) {}