package com.gigu.accessprofile.application.port.out;

import com.gigu.accessprofile.domain.model.User;

public interface TokenProviderPort {
    String generate(User user);
    String extractSubject(String token);
    boolean isValid(String token);
}
