package com.chat.infrastructure.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public JwtAuthenticationFilter(
            JwtService jwtService,
            @Lazy CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        log.debug("Auth header: {}", authHeader);

        // Nếu không có header hoặc không đúng format, chuyển tiếp request
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No valid auth header found");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            log.debug("Processing JWT token");

            // Kiểm tra token có hợp lệ không
            if (jwtService.isTokenValid(jwt, new Date())) {
                final String userId = jwtService.extractUserId(jwt);
                final Claims claims = jwtService.extractAllClaims(jwt);
                final String email = claims.getSubject();

                log.debug("Extracted user ID: {}, email: {}", userId, email);

                // Chỉ set authentication nếu chưa có trong context
                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                Collections.emptyList()  // Sử dụng empty list vì không dùng roles
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // Set authentication vào context
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("Authentication set successfully for user: {}", email);
                    } else {
                        log.warn("UserDetails is null for email: {}", email);
                        SecurityContextHolder.clearContext(); // Clear context nếu không tìm thấy user
                    }
                }
            } else {
                log.warn("Invalid JWT token");
                SecurityContextHolder.clearContext(); // Clear context khi token không hợp lệ
            }
        } catch (Exception e) {
            log.error("Failed to process JWT token", e);
            SecurityContextHolder.clearContext(); // Clear context khi có lỗi xảy ra
        }

        filterChain.doFilter(request, response);
    }
}