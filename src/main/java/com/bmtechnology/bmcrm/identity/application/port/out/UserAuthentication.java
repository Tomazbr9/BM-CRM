package com.bmtechnology.bmcrm.identity.application.port.out;

import com.bmtechnology.bmcrm.identity.application.dto.AuthenticatedUser;

public interface UserAuthentication {

    AuthenticatedUser authenticate(String email, String password);
}