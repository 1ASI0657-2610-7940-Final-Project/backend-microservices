package com.gigu.engagement.interfaces.rest;

import com.gigu.engagement.application.dto.*;
import com.gigu.engagement.application.port.in.EngagementUseCase;
import com.gigu.engagement.infrastructure.security.AuthUser;
import com.gigu.engagement.interfaces.rest.dto.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/engagement")
public class EngagementController {
    private final EngagementUseCase service;
    public EngagementController(EngagementUseCase service){this.service=service;}

    @PostMapping("/requests") @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<Map<String,Object>> createRequest(Authentication auth, @Valid @RequestBody CreateRequestBody body){
        AuthUser u=(AuthUser)auth.getPrincipal();
        var req=service.createRequest(new CreateRequestCommand(body.serviceId(),body.freelancerId(),body.message(),body.proposedPrice(),body.currency(),body.proposedDeliveryDays(),u.id(),u.roles().contains("CLIENT")));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id",req.id(),"serviceId",req.serviceId(),"clientId",req.clientId(),"freelancerId",req.freelancerId(),"status",req.status(),"proposedPrice",req.proposedPrice(),"currency",req.currency(),"createdAt", DateTimeFormatter.ISO_INSTANT.format(req.createdAt())));
    }

    @GetMapping("/requests/incoming") @SecurityRequirement(name="bearerAuth")
    public List<Map<String,Object>> incoming(Authentication auth){ AuthUser u=(AuthUser)auth.getPrincipal(); List<Map<String,Object>> out=new ArrayList<>(); for(var r:service.incoming(u.id().toString(), u.roles().contains("FREELANCER")?"FREELANCER":"CLIENT")){ Map<String,Object> m=new LinkedHashMap<>(); m.put("id",r.id());m.put("serviceId",r.serviceId());m.put("clientId",r.clientId());m.put("message",r.message());m.put("proposedPrice",r.proposedPrice());m.put("currency",r.currency());m.put("status",r.status());m.put("createdAt",DateTimeFormatter.ISO_INSTANT.format(r.createdAt())); out.add(m);} return out; }

    @GetMapping("/requests/outgoing") @SecurityRequirement(name="bearerAuth")
    public List<Map<String,Object>> outgoing(Authentication auth){ AuthUser u=(AuthUser)auth.getPrincipal(); List<Map<String,Object>> out=new ArrayList<>(); for(var r:service.outgoing(u.id().toString(), u.roles().contains("CLIENT")?"CLIENT":"FREELANCER")){ Map<String,Object> m=new LinkedHashMap<>(); m.put("id",r.id());m.put("serviceId",r.serviceId());m.put("freelancerId",r.freelancerId());m.put("proposedPrice",r.proposedPrice());m.put("currency",r.currency());m.put("status",r.status());m.put("createdAt",DateTimeFormatter.ISO_INSTANT.format(r.createdAt())); out.add(m);} return out; }

    @PatchMapping("/requests/{id}/decision") @SecurityRequirement(name="bearerAuth")
    public Map<String,Object> decision(Authentication auth,@PathVariable UUID id,@RequestBody DecisionBody body){ AuthUser u=(AuthUser)auth.getPrincipal(); var res=service.decide(id,new DecisionCommand(body.decision(),body.finalPrice(),body.finalDeliveryDays(),body.responseMessage(),u.roles().contains("FREELANCER")?"FREELANCER":"CLIENT",u.id().toString())); if(res.agreement()!=null){ return Map.of("requestId",res.requestId(),"status",res.status(),"agreement",Map.of("id",res.agreement().id(),"finalPrice",res.agreement().finalPrice(),"currency",res.agreement().currency(),"finalDeliveryDays",res.agreement().finalDeliveryDays()),"project",Map.of("id",res.project().id(),"status",res.project().status())); } return Map.of("requestId",res.requestId(),"status",res.status()); }

    @GetMapping("/projects") @SecurityRequirement(name="bearerAuth")
    public List<Map<String,Object>> projects(Authentication auth){ AuthUser u=(AuthUser)auth.getPrincipal(); List<Map<String,Object>> out=new ArrayList<>(); for(var p:service.projects(u.id().toString())){ Map<String,Object> m=new LinkedHashMap<>(); m.put("id",p.id());m.put("serviceId",p.serviceId());m.put("clientId",p.clientId());m.put("freelancerId",p.freelancerId());m.put("status",p.status());m.put("finalPrice",p.finalPrice());m.put("currency",p.currency()); out.add(m);} return out; }

    @GetMapping("/projects/{id}") @SecurityRequirement(name="bearerAuth")
    public Map<String,Object> projectDetail(Authentication auth,@PathVariable UUID id){ AuthUser u=(AuthUser)auth.getPrincipal(); var d=service.projectDetail(id,u.id().toString()); var p=d.project(); var h=d.statusHistory().stream().map(x->Map.of("status",x.status(),"changedAt",DateTimeFormatter.ISO_INSTANT.format(x.changedAt()))).toList(); return Map.of("id",p.id(),"requestId",p.requestId(),"agreementId",p.agreementId(),"serviceId",p.serviceId(),"clientId",p.clientId(),"freelancerId",p.freelancerId(),"status",p.status(),"finalPrice",p.finalPrice(),"currency",p.currency(),"statusHistory",h); }

    @PatchMapping("/projects/{id}/status") @SecurityRequirement(name="bearerAuth")
    public Map<String,Object> updateStatus(Authentication auth,@PathVariable UUID id,@RequestBody StatusBody body){ AuthUser u=(AuthUser)auth.getPrincipal(); var res=service.updateStatus(id,new UpdateStatusCommand(body.status(),body.comment(),"PARTICIPANT",u.id().toString())); return Map.of("projectId",res.projectId(),"previousStatus",res.previousStatus(),"currentStatus",res.currentStatus(),"changedAt",DateTimeFormatter.ISO_INSTANT.format(res.changedAt())); }

    @PostMapping("/projects/{id}/reviews") @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<Map<String,Object>> createReview(Authentication auth,@PathVariable UUID id,@Valid @RequestBody ReviewBody body){ AuthUser u=(AuthUser)auth.getPrincipal(); var r=service.createReview(id,new CreateReviewCommand(body.revieweeId(),body.rating(),body.comment(),u.id().toString())); return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id",r.id(),"projectId",r.projectId(),"reviewerId",r.reviewerId(),"revieweeId",r.revieweeId(),"rating",r.rating(),"comment",r.comment(),"createdAt",DateTimeFormatter.ISO_INSTANT.format(r.createdAt()))); }

    @GetMapping("/projects/{id}/reviews") @SecurityRequirement(name="bearerAuth")
    public List<Map<String,Object>> reviews(Authentication auth,@PathVariable UUID id){ AuthUser u=(AuthUser)auth.getPrincipal(); List<Map<String,Object>> out=new ArrayList<>(); for(var r:service.reviews(id,u.id().toString())){ Map<String,Object> m=new LinkedHashMap<>(); m.put("id",r.id());m.put("reviewerId",r.reviewerId());m.put("revieweeId",r.revieweeId());m.put("rating",r.rating());m.put("comment",r.comment());m.put("createdAt",DateTimeFormatter.ISO_INSTANT.format(r.createdAt())); out.add(m);} return out; }

    @PostMapping("/price-suggestions") @SecurityRequirement(name="bearerAuth")
    public Map<String,Object> suggest(@RequestBody PriceSuggestionBody body){ var s=service.suggest(new PriceSuggestionCommand(body.serviceType(),body.complexity(),body.urgency(),body.freelancerExperience())); return Map.of("suggestedMinPrice",s.suggestedMinPrice(),"suggestedMaxPrice",s.suggestedMaxPrice(),"currency",s.currency(),"criteria",Map.of("serviceType",s.serviceType(),"complexity",s.complexity(),"urgency",s.urgency(),"freelancerExperience",s.freelancerExperience())); }
}

