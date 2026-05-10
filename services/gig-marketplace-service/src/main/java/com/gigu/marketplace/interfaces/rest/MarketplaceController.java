package com.gigu.marketplace.interfaces.rest;

import com.gigu.marketplace.application.dto.*;
import com.gigu.marketplace.application.port.in.command.MarketplaceCommandUseCase;
import com.gigu.marketplace.application.port.in.query.MarketplaceQueryUseCase;
import com.gigu.marketplace.infrastructure.security.AuthUser;
import com.gigu.marketplace.interfaces.rest.dto.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/marketplace")
public class MarketplaceController {
    private final MarketplaceCommandUseCase commands; private final MarketplaceQueryUseCase queries;
    public MarketplaceController(MarketplaceCommandUseCase commands, MarketplaceQueryUseCase queries){this.commands=commands;this.queries=queries;}

    @GetMapping("/services")
    public ResponseEntity<Map<String,Object>> search(@RequestParam(required=false) String category,@RequestParam(required=false) Double priceMin,@RequestParam(required=false) Double priceMax,@RequestParam(required=false) Double minRating,@RequestParam(required=false) String q,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="10") int pageSize){
        var result=queries.search(new SearchQuery(category,priceMin,priceMax,minRating,q,page,pageSize));
        List<Map<String,Object>> data=new ArrayList<>();
        for (var s: result.data()) {
            Map<String,Object> m=new LinkedHashMap<>();
            m.put("id", s.id()); m.put("title", s.title()); m.put("descriptionPreview", s.description().length()>60?s.description().substring(0,60):s.description()); m.put("basePrice", s.basePrice()); m.put("currency", s.currency()); m.put("category", s.categoryName()); m.put("freelancerId", s.freelancerId()); m.put("freelancerDisplayName", s.freelancerDisplayName()); m.put("averageRating", 4.8); m.put("thumbnailUrl", "");
            data.add(m);
        }
        return ResponseEntity.ok(Map.of("data",data,"total",result.total(),"page",result.page(),"pageSize",result.pageSize()));
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<Map<String,Object>> detail(@PathVariable UUID id){
        var s=queries.detail(id);
        Map<String,Object> out=new LinkedHashMap<>();
        out.put("id", s.id()); out.put("title", s.title()); out.put("description", s.description()); out.put("basePrice", s.basePrice()); out.put("currency", s.currency()); out.put("deliveryDays", s.deliveryDays()); out.put("status", s.status()); out.put("category", Map.of("id", s.categoryId(), "name", s.categoryName())); out.put("tags", s.tags()); out.put("freelancer", Map.of("id", s.freelancerId(), "displayName", s.freelancerDisplayName(), "averageRating", 4.8, "reviewsCount", 24)); out.put("media", List.of());
        return ResponseEntity.ok(out);
    }

    @GetMapping("/services/mine") @SecurityRequirement(name="bearerAuth")
    public List<Map<String,Object>> mine(Authentication auth){ AuthUser u=(AuthUser)auth.getPrincipal(); List<Map<String,Object>> out=new ArrayList<>(); for(var s:queries.mine(u.id())){ Map<String,Object> m=new LinkedHashMap<>(); m.put("id",s.id());m.put("title",s.title());m.put("basePrice",s.basePrice());m.put("currency",s.currency());m.put("status",s.status());m.put("thumbnailUrl",""); out.add(m);} return out; }

    @PostMapping("/services") @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<Map<String,Object>> create(Authentication auth,@Valid @RequestBody CreateServiceRequest req){ AuthUser u=(AuthUser)auth.getPrincipal(); var s=commands.createService(new CreateServiceCommand(req.title(),req.description(),req.basePrice(),req.currency(),req.categoryId(),req.deliveryDays(),req.tags(),u.id(),"Freelancer",u.roles().contains("FREELANCER"))); return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id",s.id(),"title",s.title(),"status",s.status(),"createdAt",DateTimeFormatter.ISO_INSTANT.format(s.createdAt()))); }

    @PatchMapping("/services/{id}") @SecurityRequirement(name="bearerAuth")
    public Map<String,Object> update(Authentication auth,@PathVariable UUID id,@Valid @RequestBody UpdateServiceRequest req){ AuthUser u=(AuthUser)auth.getPrincipal(); var s=commands.updateService(id,new UpdateServiceCommand(req.basePrice(),req.deliveryDays(),req.description(),u.roles().contains("FREELANCER")?"FREELANCER":"CLIENT",u.id().toString())); return Map.of("id",s.id(),"updated",true,"updatedAt",DateTimeFormatter.ISO_INSTANT.format(s.updatedAt())); }

    @DeleteMapping("/services/{id}") @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<Void> delete(Authentication auth,@PathVariable UUID id){ AuthUser u=(AuthUser)auth.getPrincipal(); commands.softDeleteService(id,u.id().toString(),u.roles().contains("FREELANCER")?"FREELANCER":"CLIENT"); return ResponseEntity.noContent().build(); }

    @PostMapping(value="/services/{id}/media",consumes="multipart/form-data") @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<Map<String,Object>> upload(Authentication auth,@PathVariable UUID id,@RequestParam("file") MultipartFile file,@RequestParam(defaultValue="false") boolean primary) throws Exception { AuthUser u=(AuthUser)auth.getPrincipal(); var m=commands.uploadMedia(id,u.id().toString(),u.roles().contains("FREELANCER")?"FREELANCER":"CLIENT",file.getContentType(),file.getBytes(),primary); return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id",m.id(),"serviceId",m.serviceId(),"url",m.url(),"type",m.type(),"primary",m.primary())); }

    @DeleteMapping("/services/{id}/media/{mediaId}") @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<Void> deleteMedia(Authentication auth,@PathVariable UUID id,@PathVariable UUID mediaId){ AuthUser u=(AuthUser)auth.getPrincipal(); commands.deleteMedia(id,mediaId,u.id().toString(),u.roles().contains("FREELANCER")?"FREELANCER":"CLIENT"); return ResponseEntity.noContent().build(); }

    @GetMapping("/categories") public List<Map<String,Object>> categories(){ List<Map<String,Object>> out = new ArrayList<>(); for(var c:queries.categories()){ out.add(Map.of("id",c.id(),"name",c.name())); } return out; }
}
