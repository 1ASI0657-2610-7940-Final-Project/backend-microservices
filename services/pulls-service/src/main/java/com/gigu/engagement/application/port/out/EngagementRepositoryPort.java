package com.gigu.engagement.application.port.out;
import com.gigu.engagement.domain.model.*;
import java.util.*;
public interface EngagementRepositoryPort {
    ProjectRequest saveRequest(ProjectRequest request);
    Optional<ProjectRequest> findRequest(UUID id);
    List<ProjectRequest> incoming(UUID freelancerId);
    List<ProjectRequest> outgoing(UUID clientId);
    ProjectRequest updateRequest(ProjectRequest request);
    Agreement saveAgreement(Agreement agreement);
    Project saveProject(Project project);
    Optional<Project> findProject(UUID id);
    List<Project> findProjectsByParticipant(UUID userId);
    Project updateProject(Project project);
    ProjectStatusHistory saveStatusHistory(ProjectStatusHistory history);
    List<ProjectStatusHistory> projectHistory(UUID projectId);
    Review saveReview(Review review);
    boolean existsReview(UUID projectId, UUID reviewerId);
    List<Review> findReviewsByProject(UUID projectId);
}
