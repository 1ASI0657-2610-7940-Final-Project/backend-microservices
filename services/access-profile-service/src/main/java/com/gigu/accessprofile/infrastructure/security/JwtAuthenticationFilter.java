package com.gigu.accessprofile.infrastructure.security;

import com.gigu.accessprofile.application.port.out.TokenProviderPort;
import com.gigu.accessprofile.application.port.out.UserRepositoryPort;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProviderPort tokenProvider;
    private final UserRepositoryPort userRepository;

    public JwtAuthenticationFilter(TokenProviderPort tokenProvider, UserRepositoryPort userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            if (tokenProvider.isValid(token)) {
                UUID userId = UUID.fromString(tokenProvider.extractSubject(token));
                userRepository.findById(userId).ifPresent(user -> {
                    var authorities = user.roles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.name())).toList();
                    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, authorities));
                });
            }
        }
        filterChain.doFilter(request, response);
    }
}
