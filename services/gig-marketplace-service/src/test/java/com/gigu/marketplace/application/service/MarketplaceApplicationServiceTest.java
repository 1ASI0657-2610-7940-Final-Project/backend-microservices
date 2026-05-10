package com.gigu.marketplace.application.service;

import com.gigu.marketplace.application.dto.*;
import com.gigu.marketplace.application.port.out.*;
import com.gigu.marketplace.domain.model.*;
import com.gigu.marketplace.domain.valueobject.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketplaceApplicationServiceTest {
    @Mock ServiceOfferingRepositoryPort serviceRepo;
    @Mock CategoryRepositoryPort categoryRepo;
    @Mock MediaRepositoryPort mediaRepo;
    @Mock StoragePort storage;
    @InjectMocks MarketplaceApplicationService app;

    @Test void createGigValidatesFreelancerRole(){
        assertThrows(SecurityException.class, () -> app.createService(new CreateServiceCommand("t","d",BigDecimal.TEN,CurrencyCode.PEN,UUID.randomUUID(),7,List.of(),UUID.randomUUID(),"Ana",false)));
    }

    @Test void createGigRejectsInvalidPrice(){
        assertThrows(IllegalArgumentException.class, () -> app.createService(new CreateServiceCommand("t","d",BigDecimal.ZERO,CurrencyCode.PEN,UUID.randomUUID(),7,List.of(),UUID.randomUUID(),"Ana",true)));
    }

    @Test void updateRejectsNonOwner(){
        UUID sid=UUID.randomUUID(); UUID owner=UUID.randomUUID();
        when(serviceRepo.findById(sid)).thenReturn(Optional.of(new ServiceOffering(sid,owner,"Ana","t","d",BigDecimal.TEN,CurrencyCode.PEN,7,ServiceStatus.PUBLISHED,UUID.randomUUID(),"Design",List.of(),Instant.now(),Instant.now())));
        assertThrows(SecurityException.class, () -> app.updateService(sid,new UpdateServiceCommand(BigDecimal.ONE,5,"x","FREELANCER",UUID.randomUUID().toString())));
    }

    @Test void deleteDoesSoftDelete(){
        UUID sid=UUID.randomUUID(); UUID owner=UUID.randomUUID();
        ServiceOffering s=new ServiceOffering(sid,owner,"Ana","t","d",BigDecimal.TEN,CurrencyCode.PEN,7,ServiceStatus.PUBLISHED,UUID.randomUUID(),"Design",List.of(),Instant.now(),Instant.now());
        when(serviceRepo.findById(sid)).thenReturn(Optional.of(s)); when(serviceRepo.update(any())).thenAnswer(i->i.getArgument(0));
        app.softDeleteService(sid,owner.toString(),"FREELANCER");
        verify(serviceRepo).update(argThat(x -> x.status()==ServiceStatus.UNPUBLISHED));
    }

    @Test void mediaUploadValidatesOwner(){
        UUID sid=UUID.randomUUID(); UUID owner=UUID.randomUUID();
        when(serviceRepo.findById(sid)).thenReturn(Optional.of(new ServiceOffering(sid,owner,"Ana","t","d",BigDecimal.TEN,CurrencyCode.PEN,7,ServiceStatus.PUBLISHED,UUID.randomUUID(),"Design",List.of(),Instant.now(),Instant.now())));
        assertThrows(SecurityException.class, () -> app.uploadMedia(sid,UUID.randomUUID().toString(),"FREELANCER","image/png","x.png",new byte[]{1},true));
    }

    @Test void createGigSuccess() {
        UUID cat = UUID.randomUUID();
        UUID uid = UUID.randomUUID();
        when(categoryRepo.findCategoryById(cat)).thenReturn(Optional.of(new ServiceCategory(cat, "Design")));
        when(serviceRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        var created = app.createService(new CreateServiceCommand("t","d",BigDecimal.TEN,CurrencyCode.PEN,cat,7,List.of("Vue"),uid,"Ana",true));
        assertEquals(ServiceStatus.PUBLISHED, created.status());
    }

    @Test void updateOwnerSuccess() {
        UUID sid=UUID.randomUUID(); UUID owner=UUID.randomUUID(); UUID cat=UUID.randomUUID();
        var s = new ServiceOffering(sid,owner,"Ana","t","d",BigDecimal.TEN,CurrencyCode.PEN,7,ServiceStatus.PUBLISHED,cat,"Design",List.of(),Instant.now(),Instant.now());
        when(serviceRepo.findById(sid)).thenReturn(Optional.of(s));
        when(serviceRepo.update(any())).thenAnswer(i -> i.getArgument(0));
        var updated = app.updateService(sid,new UpdateServiceCommand(BigDecimal.valueOf(20),5,"new","FREELANCER",owner.toString()));
        assertEquals(BigDecimal.valueOf(20), updated.basePrice());
    }

    @Test void mediaUploadOwnerSuccess() {
        UUID sid=UUID.randomUUID(); UUID owner=UUID.randomUUID(); UUID cat=UUID.randomUUID();
        var s = new ServiceOffering(sid,owner,"Ana","t","d",BigDecimal.TEN,CurrencyCode.PEN,7,ServiceStatus.PUBLISHED,cat,"Design",List.of(),Instant.now(),Instant.now());
        when(serviceRepo.findById(sid)).thenReturn(Optional.of(s));
        when(storage.store(any(), any(), any(), any())).thenReturn(new StoragePort.Stored("gig-media","services/x/y","https://x","image/png",10));
        when(mediaRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        var media = app.uploadMedia(sid,owner.toString(),"FREELANCER","image/png","x.png",new byte[]{1},true);
        assertTrue(media.primary());
        verify(mediaRepo).clearPrimary(sid);
    }

    @Test void deleteMediaOwnerSuccess() {
        UUID sid=UUID.randomUUID(); UUID owner=UUID.randomUUID(); UUID cat=UUID.randomUUID(); UUID mediaId=UUID.randomUUID();
        var s = new ServiceOffering(sid,owner,"Ana","t","d",BigDecimal.TEN,CurrencyCode.PEN,7,ServiceStatus.PUBLISHED,cat,"Design",List.of(),Instant.now(),Instant.now());
        when(serviceRepo.findById(sid)).thenReturn(Optional.of(s));
        when(mediaRepo.findMediaById(mediaId)).thenReturn(Optional.of(new ServiceMedia(mediaId,sid,"u","IMAGE",true,"b","p","image/png",1,Instant.now())));
        app.deleteMedia(sid,mediaId,owner.toString(),"FREELANCER");
        verify(mediaRepo).delete(mediaId);
    }

    @Test void searchDetailMineAndCategories() {
        when(serviceRepo.searchPublished(any())).thenReturn(new ServiceOfferingRepositoryPort.MarketplaceSearchResult(List.of(),0));
        assertEquals(0, app.search(new SearchQuery(null,null,null,null,null,1,10)).total());
        UUID sid=UUID.randomUUID(); UUID owner=UUID.randomUUID(); UUID cat=UUID.randomUUID();
        var s = new ServiceOffering(sid,owner,"Ana","t","d",BigDecimal.TEN,CurrencyCode.PEN,7,ServiceStatus.PUBLISHED,cat,"Design",List.of(),Instant.now(),Instant.now());
        when(serviceRepo.findById(sid)).thenReturn(Optional.of(s));
        when(serviceRepo.findByFreelancerId(owner)).thenReturn(List.of(s));
        when(categoryRepo.findAll()).thenReturn(List.of(new ServiceCategory(cat,"Design")));
        assertEquals(sid, app.detail(sid).id());
        assertEquals(1, app.mine(owner).size());
        assertEquals(1, app.categories().size());
    }
}
