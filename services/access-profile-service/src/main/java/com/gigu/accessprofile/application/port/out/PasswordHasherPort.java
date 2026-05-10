package com.gigu.accessprofile.application.port.out;

public interface PasswordHasherPort {
    String hash(String raw);
    boolean matches(String raw, String encoded);
}
