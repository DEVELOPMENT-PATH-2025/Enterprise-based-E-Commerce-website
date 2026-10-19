package com.nexusmart.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/css/") || path.startsWith("/js/") || 
               path.startsWith("/images/") || path.endsWith(".html") ||
               path.equals("/") || path.startsWith("/actuator") || 
               path.startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String header = request.getHeader("Authorization");
        String token = null;
        String email = null;
        UUID userId = null;
        String role = null;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                email = jwtTokenProvider.getEmailFromToken(token);
                userId = jwtTokenProvider.getUserIdFromToken(token);
                role = jwtTokenProvider.getRoleFromToken(token);
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserPrincipal userPrincipal = new UserPrincipal(userId, email, role);
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    userPrincipal, 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority(role))
                );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
