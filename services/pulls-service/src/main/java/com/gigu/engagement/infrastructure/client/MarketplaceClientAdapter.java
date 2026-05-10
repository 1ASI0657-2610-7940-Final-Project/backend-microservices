package com.gigu.engagement.infrastructure.client;
import com.gigu.engagement.application.port.out.MarketplaceServiceClientPort;
import java.util.UUID;
import org.slf4j.*;
import org.springframework.stereotype.Component;
@Component
public class MarketplaceClientAdapter implements MarketplaceServiceClientPort {
    private static final Logger log = LoggerFactory.getLogger(MarketplaceClientAdapter.class);
    public boolean serviceExists(UUID serviceId){ return true; }
    public UUID serviceOwner(UUID serviceId){ return UUID.randomUUID(); }
    public void updateReputationBestEffort(UUID userId){ log.info("best-effort reputation update for {}", userId); }
}
