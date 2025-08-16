package com.example.money.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    @Value("${app.firebase.test-uid:}")
    private String testFirebaseUid;

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/health",
            "/actuator/health",
            "/public",
            "/swagger-ui",
            "/v3/api-docs"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (isSwaggerMode() && !testFirebaseUid.isEmpty()) {
            // In dev mode, use the configured test UID
            request.setAttribute("firebaseUid", testFirebaseUid);
            filterChain.doFilter(request, response);
            return;
        }

        // Normal Firebase token verification for non-dev profiles
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        String idToken = header.substring(7);
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            request.setAttribute("firebaseUid", decodedToken.getUid());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Firebase token: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSwaggerMode() {
        return activeProfile.contains("swagger");
    }
}