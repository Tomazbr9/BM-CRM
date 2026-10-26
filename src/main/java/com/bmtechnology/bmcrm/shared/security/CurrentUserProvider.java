package com.bmtechnology.bmcrm.shared.security;

import java.util.UUID;

public interface CurrentUserProvider {

    UUID getCurrentUserId();
}