package com.gigu.accessprofile.application.dto;

import com.gigu.accessprofile.domain.valueobject.RoleName;

public record SignUpCommand(String firstName, String lastName, String email, String password, RoleName role) {}
