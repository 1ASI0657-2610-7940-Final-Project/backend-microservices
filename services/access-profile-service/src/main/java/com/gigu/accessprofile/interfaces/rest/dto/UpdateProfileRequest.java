package com.gigu.accessprofile.interfaces.rest.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record UpdateProfileRequest(String bio, List<@NotBlank String> skills) {}
