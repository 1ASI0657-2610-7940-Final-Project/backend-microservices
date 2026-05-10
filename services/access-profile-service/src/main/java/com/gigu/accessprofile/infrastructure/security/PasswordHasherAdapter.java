package com.gigu.accessprofile.infrastructure.security;

import com.gigu.accessprofile.application.port.out.PasswordHasherPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasherAdapter implements PasswordHasherPort {
    private final PasswordEncoder passwordEncoder;
    public PasswordHasherAdapter(PasswordEncoder passwordEncoder) { this.passwordEncoder = passwordEncoder; }
    public String hash(String raw) { return passwordEncoder.encode(raw); }
    public boolean matches(String raw, String encoded) { return passwordEncoder.matches(raw, encoded); }
}
