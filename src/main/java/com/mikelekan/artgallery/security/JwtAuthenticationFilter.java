package com.mikelekan.artgallery.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * JWT Authentication Filter
 *
 * This filter runs on EVERY HTTP request before it reaches your controllers.
 * It checks for a JWT token, validates it, and authenticates the user.
 *
 * Extends OncePerRequestFilter: Guarantees this filter runs exactly once per request
 * (even if request is forwarded or included multiple times)
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * Constructor injection (recommended over @Autowired)
     *
     * @param jwtUtil Our JWT utility for extracting/validating tokens
     * @param userDetailsService Spring Security service to load users
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * This method runs on EVERY request
     *
     * Flow:
     * 1. Extract JWT token from Authorization header
     * 2. Extract username from token
     * 3. Check if user is already authenticated (optimization)
     * 4. Load user from database
     * 5. Validate token
     * 6. Set authentication in SecurityContext
     * 7. Continue filter chain
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extract Authorization header
        final String authHeader = request.getHeader("Authorization");

        // If no Authorization header or doesn't start with "Bearer ", skip this filter
        // This allows public endpoints (like login) to work without a token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract token (remove "Bearer " prefix)
        // "Bearer eyJhbGci..." → "eyJhbGci..."
        final String jwt = authHeader.substring(7);

        // 3. Extract username from token
        // This parses the JWT payload and gets the "sub" field
        final String username = jwtUtil.extractUsername(jwt);

        // 4. Check if user is NOT already authenticated
        // SecurityContextHolder.getContext().getAuthentication() returns current auth
        // If null, no one is authenticated yet for this request
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 5. Load user details from database using username from token
            // This is the User entity we created (implements UserDetails)
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 6. Validate token
            // Checks:
            // - Username in token matches the UserDetails
            // - Token is not expired
            // - Signature is valid
            if (jwtUtil.validateToken(jwt, userDetails)) {

                // 7. Create authentication token
                // This tells Spring Security: "This user is authenticated"
                //
                // UsernamePasswordAuthenticationToken is Spring Security's way
                // of representing an authenticated user
                //
                // Parameters:
                // - userDetails: Who the user is (our User entity)
                // - null: credentials (we don't need password here, already validated)
                // - userDetails.getAuthorities(): User's roles (ROLE_ADMIN, etc.)
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // 8. Set additional details (IP address, session ID, etc.)
                // This is optional but useful for logging/auditing
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 9. Set authentication in SecurityContext
                // This is THE critical step!
                // After this, Spring Security knows this request is authenticated
                // SecurityContextHolder is thread-local storage for the current request
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // Now when request reaches your controller, Spring Security knows:
                // - User is authenticated
                // - Username is "admin"
                // - User has ROLE_ADMIN authority
            }
        }

        // 10. Continue the filter chain
        // Pass request to next filter or to the controller
        // If we didn't set authentication, request continues as unauthenticated
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/");  // Skip filter for /api/auth/** endpoints
    }
}