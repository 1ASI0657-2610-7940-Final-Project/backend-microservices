package com.gigu.chatnotification.infrastructure.client;

import com.gigu.chatnotification.application.port.out.ParticipantProfilePort;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AccessProfileParticipantClientAdapter implements ParticipantProfilePort {
    private static final Logger log = LoggerFactory.getLogger(AccessProfileParticipantClientAdapter.class);

    private final RestClient restClient;
    private final String serviceToken;
    private final String accessProfileServiceUrl;

    public AccessProfileParticipantClientAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${ACCESS_PROFILE_SERVICE_URL:}") String accessProfileServiceUrl,
            @Value("${SERVICE_TOKEN:internal-token}") String serviceToken) {
        this.restClient = restClientBuilder.build();
        this.serviceToken = serviceToken;
        this.accessProfileServiceUrl = accessProfileServiceUrl == null ? "" : accessProfileServiceUrl.trim();
    }

    @Override
    public Optional<ParticipantProfileView> findByUserId(UUID userId) {
        if (accessProfileServiceUrl.isBlank()) {
            log.warn("access profile service url is blank; falling back to default participant name for userId={}", userId);
            return Optional.empty();
        }

        try {
            UserSummaryResponse response = restClient.get()
                    .uri(accessProfileServiceUrl + "/api/v1/access/internal/users/{userId}", userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("X-Service-Token", serviceToken)
                    .retrieve()
                    .body(UserSummaryResponse.class);

            if (response == null || response.id() == null) {
                return Optional.empty();
            }

            String role = response.roles() != null && !response.roles().isEmpty() ? response.roles().getFirst() : null;
            return Optional.of(new ParticipantProfileView(response.id(), response.displayName(), role, null));
        } catch (RestClientException ex) {
            log.warn("failed to fetch participant summary from access-profile-service for userId={}", userId, ex);
            return Optional.empty();
        }
    }

    private record UserSummaryResponse(UUID id, String displayName, String firstName, String lastName, String email, java.util.List<String> roles) {}
}
