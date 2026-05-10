package com.gigu.marketplace.infrastructure.config;
import com.gigu.marketplace.infrastructure.security.JwtAuthFilter;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
public class SecurityConfig {
    @Bean SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter filter) throws Exception {
        http.csrf(c->c.disable())
                .httpBasic(c->c.disable())
                .formLogin(c->c.disable())
                .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a->a.requestMatchers("/swagger-ui.html","/swagger-ui/**","/v3/api-docs/**","/actuator/health","/api/v1/marketplace/services","/api/v1/marketplace/services/**","/api/v1/marketplace/categories").permitAll().anyRequest().authenticated())
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
