package com.gigu.accessprofile.interfaces.rest.dto;

import com.gigu.accessprofile.domain.valueobject.RoleName;
import jakarta.validation.constraints.*;

public record SignUpRequest(@NotBlank String firstName, @NotBlank String lastName, @Email @NotBlank String email, @NotBlank String password, @NotNull RoleName role) {}
