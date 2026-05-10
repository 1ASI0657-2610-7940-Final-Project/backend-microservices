package com.gigu.engagement.infrastructure.security;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final byte[] secret;
    public JwtAuthFilter(@Value("${JWT_SECRET:jwt-secret-change-me-jwt-secret-change-me}") String secret){this.secret=secret.getBytes(StandardCharsets.UTF_8);}    
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String auth=req.getHeader("Authorization");
        if(auth!=null && auth.startsWith("Bearer ")){
            try{
                var claims=Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secret)).build().parseSignedClaims(auth.substring(7)).getPayload();
                UUID id=UUID.fromString(claims.getSubject());
                @SuppressWarnings("unchecked") List<String> roles=(List<String>)claims.get("roles", List.class);
                Set<String> roleSet=roles==null?Set.of():new HashSet<>(roles);
                var authorities=roleSet.stream().map(r->new SimpleGrantedAuthority("ROLE_"+r)).toList();
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new AuthUser(id, roleSet), null, authorities));
            }catch(Exception ignored){}
        }
        chain.doFilter(req,res);
    }
}
