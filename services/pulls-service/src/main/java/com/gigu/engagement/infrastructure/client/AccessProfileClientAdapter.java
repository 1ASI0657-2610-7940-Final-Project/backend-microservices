package com.gigu.engagement.infrastructure.client;
import com.gigu.engagement.application.port.out.AccessProfileClientPort;
import java.util.UUID;
import org.springframework.stereotype.Component;
@Component
public class AccessProfileClientAdapter implements AccessProfileClientPort { public boolean userExists(UUID userId){ return true; } }
