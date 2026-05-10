package com.gigu.marketplace.application.port.in.command;
import com.gigu.marketplace.application.dto.CreateServiceCommand;
import com.gigu.marketplace.application.dto.UpdateServiceCommand;
import com.gigu.marketplace.domain.model.ServiceMedia;
import com.gigu.marketplace.domain.model.ServiceOffering;
import java.util.UUID;
public interface MarketplaceCommandUseCase {
    ServiceOffering createService(CreateServiceCommand command);
    ServiceOffering updateService(UUID serviceId, UpdateServiceCommand command);
    void softDeleteService(UUID serviceId, String actorId, String actorRole);
    ServiceMedia uploadMedia(UUID serviceId, String actorId, String actorRole, String contentType, String originalFileName, byte[] bytes, boolean primary);
    void deleteMedia(UUID serviceId, UUID mediaId, String actorId, String actorRole);
}
