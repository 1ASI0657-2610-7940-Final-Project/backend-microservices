package com.gigu.accessprofile.application.dto;

import java.util.List;

public record UpdateProfileCommand(String bio, List<String> skills) {}
