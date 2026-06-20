package com.gigu.chatnotification.application.service;

import com.gigu.chatnotification.application.port.out.ParticipantProfilePort;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ParticipantDisplayNameResolver {
    private static final Logger log = LoggerFactory.getLogger(ParticipantDisplayNameResolver.class);
    private final ParticipantProfilePort participantProfiles;

    public ParticipantDisplayNameResolver(ParticipantProfilePort participantProfiles) {
        this.participantProfiles = participantProfiles;
    }

    public ParticipantView resolve(UUID userId) {
        try {
            return participantProfiles.findByUserId(userId)
                    .map(profile -> new ParticipantView(
                            profile.id(),
                            fallbackDisplayName(profile.displayName()),
                            profile.role(),
                            profile.avatarUrl()))
                    .orElseGet(() -> new ParticipantView(userId, "User", null, null));
        } catch (RuntimeException ex) {
            log.warn("failed to resolve participant display name for userId={}", userId, ex);
            return new ParticipantView(userId, "User", null, null);
        }
    }

    private String fallbackDisplayName(String displayName) {
        return displayName == null || displayName.isBlank() ? "User" : displayName.trim();
    }

    public record ParticipantView(UUID id, String displayName, String role, String avatarUrl) {}
}
