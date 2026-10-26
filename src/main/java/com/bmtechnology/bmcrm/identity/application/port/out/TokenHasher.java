package com.bmtechnology.bmcrm.identity.application.port.out;

public interface TokenHasher {
    String hash(String token);
}
