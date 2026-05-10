package com.gigu.engagement.application.port.out;
import java.util.UUID;
public interface MarketplaceServiceClientPort { boolean serviceExists(UUID serviceId); UUID serviceOwner(UUID serviceId); void updateReputationBestEffort(UUID userId); }
