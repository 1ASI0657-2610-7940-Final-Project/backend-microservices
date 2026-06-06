package com.gigu.engagement.application.service;

import com.gigu.engagement.application.dto.*;
import com.gigu.engagement.application.port.in.EngagementUseCase;
import com.gigu.engagement.application.port.out.*;
import com.gigu.engagement.domain.model.*;
import com.gigu.engagement.domain.policy.*;
import com.gigu.engagement.domain.valueobject.*;
import java.time.Instant;
import java.util.*;
import org.slf4j.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class EngagementApplicationService implements EngagementUseCase {
    private static final Logger log = LoggerFactory.getLogger(EngagementApplicationService.class);
    private final EngagementRepositoryPort repo; private final MarketplaceServiceClientPort marketplace; private final AccessProfileClientPort accessProfile; private final NotificationClientPort notifications; private final ProjectStatusPolicy statusPolicy; private final PriceSuggestionPolicy pricePolicy;
    public EngagementApplicationService(EngagementRepositoryPort repo, MarketplaceServiceClientPort marketplace, AccessProfileClientPort accessProfile, NotificationClientPort notifications, ProjectStatusPolicy statusPolicy, PriceSuggestionPolicy pricePolicy){this.repo=repo;this.marketplace=marketplace;this.accessProfile=accessProfile;this.notifications=notifications;this.statusPolicy=statusPolicy;this.pricePolicy=pricePolicy;}

    public ProjectRequest createRequest(CreateRequestCommand c){
        if(!c.clientRole()) throw new SecurityException("forbidden");
        if(c.proposedPrice()==null || c.proposedPrice().doubleValue()<=0) throw new IllegalArgumentException("invalid price");
        if(c.proposedDeliveryDays()<=0) throw new IllegalArgumentException("invalid delivery days");
        if(c.clientId().equals(c.freelancerId())) throw new IllegalArgumentException("client cannot be freelancer");
        if(!marketplace.serviceExists(c.serviceId())) throw new IllegalArgumentException("service not found");
        if(!accessProfile.userExists(c.freelancerId())) throw new IllegalArgumentException("freelancer not found");
        var req = repo.saveRequest(new ProjectRequest(UUID.randomUUID(), c.serviceId(), c.clientId(), c.freelancerId(), c.message(), c.proposedPrice(), c.currency(), c.proposedDeliveryDays(), ProjectRequestStatus.PENDING, Instant.now()));
        try { notifications.notifyBestEffort("REQUEST_CREATED", c.freelancerId().toString(), "New request", "REQUEST", req.id()); } catch (Exception e){ log.warn("notification failure", e); }
        return req;
    }

    @Transactional(readOnly = true) public List<ProjectRequest> incoming(String freelancerId, String role){ if(!"FREELANCER".equals(role)) throw new SecurityException("forbidden"); return repo.incoming(UUID.fromString(freelancerId)); }
    @Transactional(readOnly = true) public List<ProjectRequest> outgoing(String clientId, String role){ if(!"CLIENT".equals(role)) throw new SecurityException("forbidden"); return repo.outgoing(UUID.fromString(clientId)); }

    public DecisionResult decide(UUID requestId, DecisionCommand c){
        var req = repo.findRequest(requestId).orElseThrow(() -> new IllegalArgumentException("request not found"));
        if(!"FREELANCER".equals(c.actorRole()) || !req.freelancerId().toString().equals(c.actorId())) throw new SecurityException("forbidden");
        if(c.decision()==ProjectRequestStatus.ACCEPTED){
            var accepted = repo.updateRequest(new ProjectRequest(req.id(), req.serviceId(), req.clientId(), req.freelancerId(), req.message(), req.proposedPrice(), req.currency(), req.proposedDeliveryDays(), ProjectRequestStatus.ACCEPTED, req.createdAt()));
            var agr = repo.saveAgreement(new Agreement(UUID.randomUUID(), req.id(), c.finalPrice()==null?req.proposedPrice():c.finalPrice(), req.currency(), c.finalDeliveryDays()==null?req.proposedDeliveryDays():c.finalDeliveryDays(), c.responseMessage(), Instant.now()));
            var project = repo.saveProject(new Project(UUID.randomUUID(), req.id(), agr.id(), req.serviceId(), req.clientId(), req.freelancerId(), ProjectStatus.PENDING, agr.finalPrice(), agr.currency(), Instant.now(), Instant.now()));
            repo.saveStatusHistory(new ProjectStatusHistory(UUID.randomUUID(), project.id(), ProjectStatus.PENDING, "created", Instant.now()));
            try { notifications.notifyBestEffort("REQUEST_ACCEPTED", req.clientId().toString(), "Request accepted", "REQUEST", req.id()); } catch (Exception e){ log.warn("notification failure", e); }
            return new DecisionResult(accepted.id(), accepted.status().name(), agr, project);
        }
        var rejected = repo.updateRequest(new ProjectRequest(req.id(), req.serviceId(), req.clientId(), req.freelancerId(), req.message(), req.proposedPrice(), req.currency(), req.proposedDeliveryDays(), ProjectRequestStatus.REJECTED, req.createdAt()));
        try { notifications.notifyBestEffort("REQUEST_REJECTED", req.clientId().toString(), "Request rejected", "REQUEST", req.id()); } catch (Exception e){ log.warn("notification failure", e); }
        return new DecisionResult(rejected.id(), rejected.status().name(), null, null);
    }

    @Transactional(readOnly = true) public List<Project> projects(String userId){ return repo.findProjectsByParticipant(UUID.fromString(userId)); }
    @Transactional(readOnly = true) public ProjectDetail projectDetail(UUID projectId, String userId){ var p=repo.findProject(projectId).orElseThrow(() -> new IllegalArgumentException("project not found")); if(!p.clientId().toString().equals(userId)&&!p.freelancerId().toString().equals(userId)) throw new SecurityException("forbidden"); return new ProjectDetail(p, repo.projectHistory(projectId)); }

    public StatusUpdateResult updateStatus(UUID projectId, UpdateStatusCommand c){
        var p = repo.findProject(projectId).orElseThrow(() -> new IllegalArgumentException("project not found"));
        if(!p.clientId().toString().equals(c.actorId()) && !p.freelancerId().toString().equals(c.actorId())) throw new SecurityException("forbidden");
        if(!statusPolicy.canTransition(p.status(), c.status())) throw new ResponseStatusException(HttpStatus.CONFLICT, "invalid transition");
        var updated = repo.updateProject(new Project(p.id(), p.requestId(), p.agreementId(), p.serviceId(), p.clientId(), p.freelancerId(), c.status(), p.finalPrice(), p.currency(), p.createdAt(), Instant.now()));
        var history = repo.saveStatusHistory(new ProjectStatusHistory(UUID.randomUUID(), p.id(), c.status(), c.comment(), Instant.now()));
        String other = p.clientId().toString().equals(c.actorId()) ? p.freelancerId().toString() : p.clientId().toString();
        try { notifications.notifyBestEffort("PROJECT_STATUS_CHANGED", other, "Project status changed", "PROJECT", p.id()); } catch (Exception e){ log.warn("notification failure", e); }
        return new StatusUpdateResult(updated.id(), p.status().name(), updated.status().name(), history.changedAt());
    }

    public Review createReview(UUID projectId, CreateReviewCommand c){
        var p=repo.findProject(projectId).orElseThrow(() -> new IllegalArgumentException("project not found"));
        if(p.status()!=ProjectStatus.FINISHED) throw new IllegalArgumentException("project not finished");
        UUID reviewer=UUID.fromString(c.actorId());
        if(!reviewer.equals(p.clientId()) && !reviewer.equals(p.freelancerId())) throw new SecurityException("forbidden");
        UUID expectedOther = reviewer.equals(p.clientId()) ? p.freelancerId() : p.clientId();
        if(!expectedOther.equals(c.revieweeId())) throw new IllegalArgumentException("invalid reviewee");
        if(c.rating()<1 || c.rating()>5) throw new IllegalArgumentException("invalid rating");
        if(repo.existsReview(projectId, reviewer)) throw new IllegalArgumentException("duplicate review");
        var saved = repo.saveReview(new Review(UUID.randomUUID(), projectId, reviewer, c.revieweeId(), c.rating(), c.comment(), Instant.now()));
        try { notifications.notifyBestEffort("REVIEW_CREATED", c.revieweeId().toString(), "New review", "PROJECT", p.id()); } catch (Exception e){ log.warn("notification failure", e); }
        try { marketplace.updateReputationBestEffort(c.revieweeId()); } catch (Exception e){ log.warn("reputation update failed", e); }
        return saved;
    }

    @Transactional(readOnly = true) public List<Review> reviews(UUID projectId, String userId){ var p=repo.findProject(projectId).orElseThrow(() -> new IllegalArgumentException("project not found")); if(!p.clientId().toString().equals(userId)&&!p.freelancerId().toString().equals(userId)) throw new SecurityException("forbidden"); return repo.findReviewsByProject(projectId); }
    @Transactional(readOnly = true) public PriceSuggestion suggest(PriceSuggestionCommand c){ return pricePolicy.suggest(c.serviceType(), c.complexity(), c.urgency(), c.freelancerExperience()); }
}
