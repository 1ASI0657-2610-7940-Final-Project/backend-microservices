package com.gigu.chatnotification.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface ParticipantProfilePort {
    Optional<ParticipantProfileView> findByUserId(UUID userId);

    record ParticipantProfileView(UUID id, String displayName, String role, String avatarUrl) {}
}
