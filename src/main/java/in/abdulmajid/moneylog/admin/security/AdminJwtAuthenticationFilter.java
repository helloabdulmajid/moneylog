package in.abdulmajid.moneylog.admin.security;

import in.abdulmajid.moneylog.admin.model.Admin;
import in.abdulmajid.moneylog.admin.model.AdminPermission;
import in.abdulmajid.moneylog.admin.repository.AdminRepository;
import in.abdulmajid.moneylog.admin.service.AdminSessionService;
import in.abdulmajid.moneylog.auth.security.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Authenticates admin access tokens for /admin/** only. Admin tokens carry
 * type=admin_access and are rejected everywhere else (the user filter only
 * accepts type=access). Authorities are loaded from the role definition in
 * this request; the JWT carries no permissions to trust.
 */
@Component
@RequiredArgsConstructor
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AdminRepository adminRepository;
    private final AdminSessionService adminSessionService;

    @Value("${admin.session.inactivity-timeout}")
    private long adminInactivityTimeoutMs;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!path.startsWith("/admin")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.isAdminAccessToken(token)) {
            // A user token on /admin/** is left to the authorization rules;
            // an invalid/garbage token is rejected by the entry point.
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtTokenProvider.extractEmail(token);
        if (email == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        Admin admin = adminRepository.findByEmail(email).orElse(null);
        if (admin == null || !Boolean.TRUE.equals(admin.getActive())) {
            writeUnauthorized(response);
            return;
        }

        String sid = jwtTokenProvider.extractSessionId(token);
        UUID sessionId = null;
        if (sid != null) {
            try {
                sessionId = UUID.fromString(sid);
            } catch (IllegalArgumentException e) {
                sessionId = null;
            }
        }

        AdminSessionService.AdminSessionStatus status = sessionId == null
                ? AdminSessionService.AdminSessionStatus.NOT_FOUND
                : adminSessionService.checkAccess(
                        sessionId, email, LocalDateTime.now(), adminInactivityTimeoutMs);

        if (status != AdminSessionService.AdminSessionStatus.VALID) {
            writeUnauthorized(response);
            return;
        }

        adminSessionService.touchActivityIfStale(sessionId, LocalDateTime.now());

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ADMIN"));
        for (AdminPermission permission : admin.getRole().permissions()) {
            authorities.add(new SimpleGrantedAuthority(permission.authority()));
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(admin, null, authorities);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Unauthorized\"}");
    }
}
