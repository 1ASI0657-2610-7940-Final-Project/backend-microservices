package com.gigu.engagement.infrastructure.client;
import com.gigu.engagement.application.port.out.NotificationClientPort;
import org.slf4j.*;
import org.springframework.stereotype.Component;
@Component
public class NotificationClientAdapter implements NotificationClientPort {
    private static final Logger log = LoggerFactory.getLogger(NotificationClientAdapter.class);
    public void notifyBestEffort(String type, String recipientId, String message){ log.info("notify {} {}", type, recipientId); }
}
