package com.gigu.engagement.application.service;

import com.gigu.engagement.application.dto.*;
import com.gigu.engagement.application.port.out.*;
import com.gigu.engagement.domain.model.*;
import com.gigu.engagement.domain.policy.*;
import com.gigu.engagement.domain.valueobject.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EngagementApplicationServiceTest {
    @Mock EngagementRepositoryPort repo;
    @Mock MarketplaceServiceClientPort marketplace;
    @Mock AccessProfileClientPort access;
    @Mock NotificationClientPort notifications;
    PriceSuggestionPolicy pricePolicy;
    ProjectStatusPolicy statusPolicy;
    EngagementApplicationService service;

    @BeforeEach
    void setUp() {
        pricePolicy = new DeterministicPriceSuggestionPolicy();
        statusPolicy = new ProjectStatusPolicy();
        service = new EngagementApplicationService(repo, marketplace, access, notifications, statusPolicy, pricePolicy);
    }

    @Test void clientCreatesRequest(){
        UUID serviceId=UUID.randomUUID(), client=UUID.randomUUID(), freelancer=UUID.randomUUID();
        when(marketplace.serviceExists(serviceId)).thenReturn(true);
        when(access.userExists(freelancer)).thenReturn(true);
        when(repo.saveRequest(any())).thenAnswer(i->i.getArgument(0));
        var r=service.createRequest(new CreateRequestCommand(serviceId,freelancer,"m",BigDecimal.valueOf(200),CurrencyCode.PEN,7,client,true));
        assertEquals(ProjectRequestStatus.PENDING,r.status());
    }
    @Test void requestRequiresClientRole(){
        assertThrows(SecurityException.class, () -> service.createRequest(new CreateRequestCommand(UUID.randomUUID(),UUID.randomUUID(),"m",BigDecimal.TEN,CurrencyCode.PEN,7,UUID.randomUUID(),false)));
    }
    @Test void requestInvalidDeliveryDays(){
        assertThrows(IllegalArgumentException.class, () -> service.createRequest(new CreateRequestCommand(UUID.randomUUID(),UUID.randomUUID(),"m",BigDecimal.TEN,CurrencyCode.PEN,0,UUID.randomUUID(),true)));
    }
    @Test void requestClientCannotBeFreelancer(){
        UUID u=UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> service.createRequest(new CreateRequestCommand(UUID.randomUUID(),u,"m",BigDecimal.TEN,CurrencyCode.PEN,2,u,true)));
    }
    @Test void requestServiceMustExist(){
        UUID s=UUID.randomUUID(), c=UUID.randomUUID(), f=UUID.randomUUID();
        when(marketplace.serviceExists(s)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.createRequest(new CreateRequestCommand(s,f,"m",BigDecimal.TEN,CurrencyCode.PEN,2,c,true)));
    }
    @Test void requestFreelancerMustExist(){
        UUID s=UUID.randomUUID(), c=UUID.randomUUID(), f=UUID.randomUUID();
        when(marketplace.serviceExists(s)).thenReturn(true); when(access.userExists(f)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.createRequest(new CreateRequestCommand(s,f,"m",BigDecimal.TEN,CurrencyCode.PEN,2,c,true)));
    }

    @Test void freelancerReceivesIncoming(){ UUID f=UUID.randomUUID(); when(repo.incoming(f)).thenReturn(List.of()); assertEquals(0,service.incoming(f.toString(),"FREELANCER").size()); }
    @Test void clientReceivesOutgoing(){ UUID c=UUID.randomUUID(); when(repo.outgoing(c)).thenReturn(List.of()); assertEquals(0,service.outgoing(c.toString(),"CLIENT").size()); }
    @Test void incomingForbiddenForClient(){ assertThrows(SecurityException.class, () -> service.incoming(UUID.randomUUID().toString(),"CLIENT")); }
    @Test void outgoingForbiddenForFreelancer(){ assertThrows(SecurityException.class, () -> service.outgoing(UUID.randomUUID().toString(),"FREELANCER")); }

    @Test void acceptRequestUsesOriginalValuesWhenFinalTermsAreNull(){
        UUID reqId=UUID.randomUUID(), sid=UUID.randomUUID(), cid=UUID.randomUUID(), fid=UUID.randomUUID();
        BigDecimal proposedPrice = BigDecimal.valueOf(200);
        int proposedDays = 7;
        ProjectRequest pr=new ProjectRequest(reqId,sid,cid,fid,"m",proposedPrice,CurrencyCode.PEN,proposedDays,ProjectRequestStatus.PENDING,Instant.now());
        when(repo.findRequest(reqId)).thenReturn(Optional.of(pr));
        when(repo.updateRequest(any())).thenAnswer(i->i.getArgument(0));
        when(repo.saveAgreement(any())).thenAnswer(i->i.getArgument(0));
        when(repo.saveProject(any())).thenAnswer(i->i.getArgument(0));

        var res=service.decide(reqId,new DecisionCommand(ProjectRequestStatus.ACCEPTED,null,null,"ok","FREELANCER",fid.toString()));

        assertEquals(ProjectRequestStatus.ACCEPTED.name(), res.status());
        assertNotNull(res.agreement());
        assertNotNull(res.project());
        assertEquals(proposedPrice, res.agreement().finalPrice());
        assertEquals(proposedDays, res.agreement().finalDeliveryDays());
        assertEquals(proposedPrice, res.project().finalPrice());
    }

    @Test void acceptRequestCreatesInitialStatusHistory(){
        UUID reqId=UUID.randomUUID(), sid=UUID.randomUUID(), cid=UUID.randomUUID(), fid=UUID.randomUUID();
        ProjectRequest pr=new ProjectRequest(reqId,sid,cid,fid,"m",BigDecimal.valueOf(200),CurrencyCode.PEN,7,ProjectRequestStatus.PENDING,Instant.now());
        when(repo.findRequest(reqId)).thenReturn(Optional.of(pr));
        when(repo.updateRequest(any())).thenAnswer(i->i.getArgument(0));
        when(repo.saveAgreement(any())).thenAnswer(i->i.getArgument(0));
        when(repo.saveProject(any())).thenAnswer(i->i.getArgument(0));
        when(repo.saveStatusHistory(any())).thenAnswer(i->i.getArgument(0));

        var res=service.decide(reqId,new DecisionCommand(ProjectRequestStatus.ACCEPTED,BigDecimal.valueOf(250),10,"ok","FREELANCER",fid.toString()));

        assertNotNull(res.project());
        verify(repo).saveProject(any());
        ArgumentCaptor<ProjectStatusHistory> historyCaptor = ArgumentCaptor.forClass(ProjectStatusHistory.class);
        verify(repo, times(1)).saveStatusHistory(historyCaptor.capture());
        assertEquals(res.project().id(), historyCaptor.getValue().projectId());
        assertEquals(ProjectStatus.PENDING, historyCaptor.getValue().status());
        assertEquals("created", historyCaptor.getValue().comment());
    }

    @Test void rejectRequestDoesNotCreateAgreementOrProject(){
        UUID reqId=UUID.randomUUID(), sid=UUID.randomUUID(), cid=UUID.randomUUID(), fid=UUID.randomUUID();
        ProjectRequest pr=new ProjectRequest(reqId,sid,cid,fid,"m",BigDecimal.valueOf(200),CurrencyCode.PEN,7,ProjectRequestStatus.PENDING,Instant.now());
        when(repo.findRequest(reqId)).thenReturn(Optional.of(pr));
        when(repo.updateRequest(any())).thenAnswer(i->i.getArgument(0));

        var res=service.decide(reqId,new DecisionCommand(ProjectRequestStatus.REJECTED,null,null,"no","FREELANCER",fid.toString()));

        assertEquals(ProjectRequestStatus.REJECTED.name(), res.status());
        assertNull(res.agreement());
        assertNull(res.project());
        verify(repo, never()).saveAgreement(any());
        verify(repo, never()).saveProject(any());
        verify(repo, never()).saveStatusHistory(any());
    }
    @Test void decisionForbiddenIfNotAssignedFreelancer(){
        UUID reqId=UUID.randomUUID(), sid=UUID.randomUUID(), cid=UUID.randomUUID(), fid=UUID.randomUUID();
        ProjectRequest pr=new ProjectRequest(reqId,sid,cid,fid,"m",BigDecimal.valueOf(200),CurrencyCode.PEN,7,ProjectRequestStatus.PENDING,Instant.now());
        when(repo.findRequest(reqId)).thenReturn(Optional.of(pr));
        assertThrows(SecurityException.class, () -> service.decide(reqId,new DecisionCommand(ProjectRequestStatus.REJECTED,null,null,"no","FREELANCER",UUID.randomUUID().toString())));
    }

    @Test void invalidStatusTransitionConflict(){
        UUID p=UUID.randomUUID(), cid=UUID.randomUUID(), fid=UUID.randomUUID();
        Project pr=new Project(p,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),cid,fid,ProjectStatus.FINISHED,BigDecimal.TEN,CurrencyCode.PEN,Instant.now(),Instant.now());
        when(repo.findProject(p)).thenReturn(Optional.of(pr));
        assertThrows(ResponseStatusException.class,()->service.updateStatus(p,new UpdateStatusCommand(ProjectStatus.IN_PROGRESS,"x","PARTICIPANT",cid.toString())));
    }
    @Test void updateStatusNotifiesOnlyTheOtherParticipant_whenClientUpdates(){
        UUID p=UUID.randomUUID(), cid=UUID.randomUUID(), fid=UUID.randomUUID();
        Project pr=new Project(p,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),cid,fid,ProjectStatus.IN_PROGRESS,BigDecimal.TEN,CurrencyCode.PEN,Instant.now(),Instant.now());
        when(repo.findProject(p)).thenReturn(Optional.of(pr));
        when(repo.updateProject(any())).thenAnswer(i->i.getArgument(0));
        when(repo.saveStatusHistory(any())).thenAnswer(i->i.getArgument(0));

        var res=service.updateStatus(p,new UpdateStatusCommand(ProjectStatus.DELIVERED,"done","PARTICIPANT",cid.toString()));

        assertEquals("DELIVERED", res.currentStatus());
        verify(repo).saveStatusHistory(argThat(history -> history.projectId().equals(p) && history.status() == ProjectStatus.DELIVERED && "done".equals(history.comment())));
        verify(notifications).notifyBestEffort(eq("PROJECT_STATUS_CHANGED"), eq(fid.toString()), eq("Project status changed"), eq("PROJECT"), eq(p));
        verify(notifications, never()).notifyBestEffort(eq("PROJECT_STATUS_CHANGED"), eq(cid.toString()), any(), any(), any());
    }

    @Test void updateStatusNotifiesOnlyTheOtherParticipant_whenFreelancerUpdates(){
        UUID p=UUID.randomUUID(), cid=UUID.randomUUID(), fid=UUID.randomUUID();
        Project pr=new Project(p,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),cid,fid,ProjectStatus.IN_PROGRESS,BigDecimal.TEN,CurrencyCode.PEN,Instant.now(),Instant.now());
        when(repo.findProject(p)).thenReturn(Optional.of(pr));
        when(repo.updateProject(any())).thenAnswer(i->i.getArgument(0));
        when(repo.saveStatusHistory(any())).thenAnswer(i->i.getArgument(0));

        var res=service.updateStatus(p,new UpdateStatusCommand(ProjectStatus.CANCELLED,"blocked","PARTICIPANT",fid.toString()));

        assertEquals("CANCELLED", res.currentStatus());
        verify(repo).saveStatusHistory(argThat(history -> history.projectId().equals(p) && history.status() == ProjectStatus.CANCELLED && "blocked".equals(history.comment())));
        verify(notifications).notifyBestEffort(eq("PROJECT_STATUS_CHANGED"), eq(cid.toString()), eq("Project status changed"), eq("PROJECT"), eq(p));
        verify(notifications, never()).notifyBestEffort(eq("PROJECT_STATUS_CHANGED"), eq(fid.toString()), any(), any(), any());
    }
    @Test void projectDetailRequiresParticipant(){
        UUID p=UUID.randomUUID(), cid=UUID.randomUUID(), fid=UUID.randomUUID();
        Project pr=new Project(p,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),cid,fid,ProjectStatus.PENDING,BigDecimal.TEN,CurrencyCode.PEN,Instant.now(),Instant.now());
        when(repo.findProject(p)).thenReturn(Optional.of(pr));
        assertThrows(SecurityException.class, () -> service.projectDetail(p, UUID.randomUUID().toString()));
    }
    @Test void updateStatusRequiresParticipant(){
        UUID p=UUID.randomUUID(), cid=UUID.randomUUID(), fid=UUID.randomUUID();
        Project pr=new Project(p,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),cid,fid,ProjectStatus.PENDING,BigDecimal.TEN,CurrencyCode.PEN,Instant.now(),Instant.now());
        when(repo.findProject(p)).thenReturn(Optional.of(pr));
        assertThrows(SecurityException.class, () -> service.updateStatus(p,new UpdateStatusCommand(ProjectStatus.IN_PROGRESS,"x","PARTICIPANT",UUID.randomUUID().toString())));
    }

    @Test void reviewRequiresFinishedProject(){
        UUID p=UUID.randomUUID(), cid=UUID.randomUUID(), fid=UUID.randomUUID();
        Project pr=new Project(p,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),cid,fid,ProjectStatus.IN_PROGRESS,BigDecimal.TEN,CurrencyCode.PEN,Instant.now(),Instant.now());
        when(repo.findProject(p)).thenReturn(Optional.of(pr));
        assertThrows(IllegalArgumentException.class,()->service.createReview(p,new CreateReviewCommand(fid,5,"ok",cid.toString())));
    }

    @Test void duplicateReviewRejected(){
        UUID p=UUID.randomUUID(), cid=UUID.randomUUID(), fid=UUID.randomUUID();
        Project pr=new Project(p,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),cid,fid,ProjectStatus.FINISHED,BigDecimal.TEN,CurrencyCode.PEN,Instant.now(),Instant.now());
        when(repo.findProject(p)).thenReturn(Optional.of(pr)); when(repo.existsReview(p,cid)).thenReturn(true);
        assertThrows(IllegalArgumentException.class,()->service.createReview(p,new CreateReviewCommand(fid,5,"ok",cid.toString())));
    }
    @Test void reviewSuccessAndReadReviews(){
        UUID p=UUID.randomUUID(), cid=UUID.randomUUID(), fid=UUID.randomUUID();
        Project pr=new Project(p,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),cid,fid,ProjectStatus.FINISHED,BigDecimal.TEN,CurrencyCode.PEN,Instant.now(),Instant.now());
        when(repo.findProject(p)).thenReturn(Optional.of(pr)); when(repo.existsReview(p,cid)).thenReturn(false); when(repo.saveReview(any())).thenAnswer(i->i.getArgument(0)); when(repo.findReviewsByProject(p)).thenReturn(List.of(new Review(UUID.randomUUID(),p,cid,fid,5,"ok",Instant.now())));
        var saved=service.createReview(p,new CreateReviewCommand(fid,5,"ok",cid.toString()));
        assertEquals(5, saved.rating());
        assertEquals(1, service.reviews(p, cid.toString()).size());
    }
    @Test void reviewInvalidRevieweeRejected(){
        UUID p=UUID.randomUUID(), cid=UUID.randomUUID(), fid=UUID.randomUUID();
        Project pr=new Project(p,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),cid,fid,ProjectStatus.FINISHED,BigDecimal.TEN,CurrencyCode.PEN,Instant.now(),Instant.now());
        when(repo.findProject(p)).thenReturn(Optional.of(pr));
        assertThrows(IllegalArgumentException.class,()->service.createReview(p,new CreateReviewCommand(UUID.randomUUID(),5,"ok",cid.toString())));
    }
    @Test void reviewsRequireParticipant(){
        UUID p=UUID.randomUUID(), cid=UUID.randomUUID(), fid=UUID.randomUUID();
        Project pr=new Project(p,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),cid,fid,ProjectStatus.FINISHED,BigDecimal.TEN,CurrencyCode.PEN,Instant.now(),Instant.now());
        when(repo.findProject(p)).thenReturn(Optional.of(pr));
        assertThrows(SecurityException.class, () -> service.reviews(p, UUID.randomUUID().toString()));
    }
    @Test void reviewInvalidRatingRejected(){
        UUID p=UUID.randomUUID(), cid=UUID.randomUUID(), fid=UUID.randomUUID();
        Project pr=new Project(p,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),cid,fid,ProjectStatus.FINISHED,BigDecimal.TEN,CurrencyCode.PEN,Instant.now(),Instant.now());
        when(repo.findProject(p)).thenReturn(Optional.of(pr));
        assertThrows(IllegalArgumentException.class,()->service.createReview(p,new CreateReviewCommand(fid,6,"ok",cid.toString())));
    }

    @Test void reviewReputationUpdateFailureDoesNotRollbackReview(){
        UUID p=UUID.randomUUID(), cid=UUID.randomUUID(), fid=UUID.randomUUID();
        Project pr=new Project(p,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),cid,fid,ProjectStatus.FINISHED,BigDecimal.TEN,CurrencyCode.PEN,Instant.now(),Instant.now());
        Review saved = new Review(UUID.randomUUID(), p, cid, fid, 5, "ok", Instant.now());
        when(repo.findProject(p)).thenReturn(Optional.of(pr));
        when(repo.existsReview(p,cid)).thenReturn(false);
        when(repo.saveReview(any())).thenReturn(saved);
        doThrow(new RuntimeException("marketplace down")).when(marketplace).updateReputationBestEffort(fid);

        var result = service.createReview(p,new CreateReviewCommand(fid,5,"ok",cid.toString()));

        assertEquals(saved, result);
        verify(repo).saveReview(any());
        verify(marketplace).updateReputationBestEffort(fid);
    }

    @Test void priceSuggestionDeterministic(){
        var s=service.suggest(new PriceSuggestionCommand("LANDING_PAGE","MEDIUM","NORMAL","INTERMEDIATE"));
        assertEquals(BigDecimal.valueOf(132.00).setScale(2), s.suggestedMinPrice());
    }

    @Test void notificationFailureDoesNotRollback(){
        UUID serviceId=UUID.randomUUID(), client=UUID.randomUUID(), freelancer=UUID.randomUUID();
        when(marketplace.serviceExists(serviceId)).thenReturn(true); when(access.userExists(freelancer)).thenReturn(true); when(repo.saveRequest(any())).thenAnswer(i->i.getArgument(0)); doThrow(new RuntimeException("down")).when(notifications).notifyBestEffort(any(), any(), any(), any(), any());
        var r=service.createRequest(new CreateRequestCommand(serviceId,freelancer,"m",BigDecimal.valueOf(200),CurrencyCode.PEN,7,client,true));
        assertNotNull(r.id());
    }
}
