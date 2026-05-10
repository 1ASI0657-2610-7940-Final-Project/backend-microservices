package com.gigu.accessprofile.interfaces.rest.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
