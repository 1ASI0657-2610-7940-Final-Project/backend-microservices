package com.gigu.engagement.infrastructure.persistence.adapter;

import com.gigu.engagement.application.port.out.EngagementRepositoryPort;
import com.gigu.engagement.domain.model.*;
import com.gigu.engagement.domain.valueobject.*;
import com.gigu.engagement.infrastructure.persistence.entity.*;
import com.gigu.engagement.infrastructure.persistence.repository.*;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class EngagementRepositoryAdapter implements EngagementRepositoryPort {
    private final ProjectRequestJpaRepository requests; private final AgreementJpaRepository agreements; private final ProjectJpaRepository projects; private final ProjectStatusHistoryJpaRepository history; private final ReviewJpaRepository reviews;
    public EngagementRepositoryAdapter(ProjectRequestJpaRepository requests, AgreementJpaRepository agreements, ProjectJpaRepository projects, ProjectStatusHistoryJpaRepository history, ReviewJpaRepository reviews){this.requests=requests;this.agreements=agreements;this.projects=projects;this.history=history;this.reviews=reviews;}

    public ProjectRequest saveRequest(ProjectRequest r){ return toDomain(requests.save(toEntity(r))); }
    public Optional<ProjectRequest> findRequest(UUID id){ return requests.findById(id).map(this::toDomain); }
    public List<ProjectRequest> incoming(UUID freelancerId){ return requests.findByFreelancerId(freelancerId).stream().map(this::toDomain).toList(); }
    public List<ProjectRequest> outgoing(UUID clientId){ return requests.findByClientId(clientId).stream().map(this::toDomain).toList(); }
    public ProjectRequest updateRequest(ProjectRequest r){ return toDomain(requests.save(toEntity(r))); }

    public Agreement saveAgreement(Agreement a){ AgreementEntity e=new AgreementEntity(); e.id=a.id();e.requestId=a.requestId();e.finalPrice=a.finalPrice();e.currency=a.currency().name();e.finalDeliveryDays=a.finalDeliveryDays();e.responseMessage=a.responseMessage();e.createdAt=a.createdAt(); var s=agreements.save(e); return new Agreement(s.id,s.requestId,s.finalPrice,CurrencyCode.valueOf(s.currency),s.finalDeliveryDays,s.responseMessage,s.createdAt); }

    public Project saveProject(Project p){ return toProject(projects.save(toProjectEntity(p))); }
    public Optional<Project> findProject(UUID id){ return projects.findById(id).map(this::toProject); }
    public List<Project> findProjectsByParticipant(UUID userId){ return projects.findByParticipant(userId).stream().map(this::toProject).toList(); }
    public Project updateProject(Project p){ return toProject(projects.save(toProjectEntity(p))); }

    public ProjectStatusHistory saveStatusHistory(ProjectStatusHistory h){ ProjectStatusHistoryEntity e=new ProjectStatusHistoryEntity(); e.id=h.id();e.projectId=h.projectId();e.status=h.status().name();e.comment=h.comment();e.changedAt=h.changedAt(); var s=history.save(e); return new ProjectStatusHistory(s.id,s.projectId,ProjectStatus.valueOf(s.status),s.comment,s.changedAt); }
    public List<ProjectStatusHistory> projectHistory(UUID projectId){ return history.findByProjectIdOrderByChangedAtAsc(projectId).stream().map(h->new ProjectStatusHistory(h.id,h.projectId,ProjectStatus.valueOf(h.status),h.comment,h.changedAt)).toList(); }

    public Review saveReview(Review r){ ReviewEntity e=new ReviewEntity(); e.id=r.id();e.projectId=r.projectId();e.reviewerId=r.reviewerId();e.revieweeId=r.revieweeId();e.rating=r.rating();e.comment=r.comment();e.createdAt=r.createdAt(); var s=reviews.save(e); return new Review(s.id,s.projectId,s.reviewerId,s.revieweeId,s.rating,s.comment,s.createdAt); }
    public boolean existsReview(UUID projectId, UUID reviewerId){ return reviews.existsByProjectIdAndReviewerId(projectId, reviewerId); }
    public List<Review> findReviewsByProject(UUID projectId){ return reviews.findByProjectId(projectId).stream().map(r->new Review(r.id,r.projectId,r.reviewerId,r.revieweeId,r.rating,r.comment,r.createdAt)).toList(); }

    private ProjectRequestEntity toEntity(ProjectRequest r){ ProjectRequestEntity e=new ProjectRequestEntity(); e.id=r.id();e.serviceId=r.serviceId();e.clientId=r.clientId();e.freelancerId=r.freelancerId();e.message=r.message();e.proposedPrice=r.proposedPrice();e.currency=r.currency().name();e.proposedDeliveryDays=r.proposedDeliveryDays();e.status=r.status().name();e.createdAt=r.createdAt(); return e; }
    private ProjectRequest toDomain(ProjectRequestEntity e){ return new ProjectRequest(e.id,e.serviceId,e.clientId,e.freelancerId,e.message,e.proposedPrice,CurrencyCode.valueOf(e.currency),e.proposedDeliveryDays,ProjectRequestStatus.valueOf(e.status),e.createdAt); }
    private ProjectEntity toProjectEntity(Project p){ ProjectEntity e=new ProjectEntity(); e.id=p.id();e.requestId=p.requestId();e.agreementId=p.agreementId();e.serviceId=p.serviceId();e.clientId=p.clientId();e.freelancerId=p.freelancerId();e.status=p.status().name();e.finalPrice=p.finalPrice();e.currency=p.currency().name();e.createdAt=p.createdAt();e.updatedAt=p.updatedAt(); return e; }
    private Project toProject(ProjectEntity e){ return new Project(e.id,e.requestId,e.agreementId,e.serviceId,e.clientId,e.freelancerId,ProjectStatus.valueOf(e.status),e.finalPrice,CurrencyCode.valueOf(e.currency),e.createdAt,e.updatedAt); }
}
