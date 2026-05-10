package com.gigu.engagement.application.port.in;
import com.gigu.engagement.application.dto.*;
import com.gigu.engagement.domain.model.*;
import java.util.*;
public interface EngagementUseCase {
    ProjectRequest createRequest(CreateRequestCommand command);
    List<ProjectRequest> incoming(String freelancerId, String role);
    List<ProjectRequest> outgoing(String clientId, String role);
    DecisionResult decide(UUID requestId, DecisionCommand command);
    List<Project> projects(String userId);
    ProjectDetail projectDetail(UUID projectId, String userId);
    StatusUpdateResult updateStatus(UUID projectId, UpdateStatusCommand command);
    Review createReview(UUID projectId, CreateReviewCommand command);
    List<Review> reviews(UUID projectId, String userId);
    PriceSuggestion suggest(PriceSuggestionCommand command);
    record DecisionResult(UUID requestId, String status, Agreement agreement, Project project) {}
    record ProjectDetail(Project project, List<ProjectStatusHistory> statusHistory) {}
    record StatusUpdateResult(UUID projectId, String previousStatus, String currentStatus, java.time.Instant changedAt) {}
}
