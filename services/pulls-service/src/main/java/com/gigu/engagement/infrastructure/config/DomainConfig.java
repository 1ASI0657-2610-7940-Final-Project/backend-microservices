package com.gigu.engagement.infrastructure.config;
import com.gigu.engagement.domain.policy.ProjectStatusPolicy;
import org.springframework.context.annotation.*;
@Configuration
public class DomainConfig { @Bean ProjectStatusPolicy projectStatusPolicy(){ return new ProjectStatusPolicy(); } }
