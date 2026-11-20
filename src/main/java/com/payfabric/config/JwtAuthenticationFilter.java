
package com.payfabric.config;

import com.payfabric.user.entity.User;
import com.payfabric.user.entity.UserStatus;
import com.payfabric.user.repository.UserRepository;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);



        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No Bearer token found.");
            logAuthenticationState();
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        try {
            if (token.isBlank()) {
                System.out.println("Token is blank.");
                logAuthenticationState();
                filterChain.doFilter(request, response);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                boolean tokenValid = jwtService.isTokenValid(token);
                System.out.println("Token valid: " + tokenValid);

                if (!tokenValid) {
                    SecurityContextHolder.clearContext();
                    logAuthenticationState();
                    filterChain.doFilter(request, response);
                    return;
                }

                UUID userId = jwtService.extractUserId(token);

                User user = userRepository.findById(userId).orElse(null);


                if (user != null) {
                    System.out.println("User status: " + user.getStatus());
                }

                if (user != null && user.getStatus() == UserStatus.ACTIVE) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    List.of()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);


                }
            }

        } catch (JwtException | IllegalArgumentException ex) {
            System.out.println("JWT processing error: "
                    + ex.getClass().getSimpleName());
            SecurityContextHolder.clearContext();
        }

        logAuthenticationState();

        filterChain.doFilter(request, response);
    }

    private void logAuthenticationState() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

    }
}
