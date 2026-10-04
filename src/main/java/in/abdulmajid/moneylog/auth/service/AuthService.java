package in.abdulmajid.moneylog.auth.service;

import in.abdulmajid.moneylog.auth.dto.request.LoginRequest;
import in.abdulmajid.moneylog.auth.dto.request.RegisterRequest;
import in.abdulmajid.moneylog.auth.dto.response.AuthResponse;
import in.abdulmajid.moneylog.auth.exception.EmailNotVerifiedException;
import in.abdulmajid.moneylog.auth.exception.InvalidRefreshTokenException;
import in.abdulmajid.moneylog.auth.model.EmailVerificationToken;
import in.abdulmajid.moneylog.auth.model.PasswordResetToken;
import in.abdulmajid.moneylog.auth.model.Session;
import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.auth.repository.EmailVerificationTokenRepository;
import in.abdulmajid.moneylog.auth.repository.PasswordResetTokenRepository;
import in.abdulmajid.moneylog.auth.repository.SessionRepository;
import in.abdulmajid.moneylog.auth.repository.UserRepository;
import in.abdulmajid.moneylog.auth.security.CustomUserDetailsService;
import in.abdulmajid.moneylog.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String INVALID_RESET_LINK = "This password reset link is invalid or has expired.";
    private static final String RESEND_GENERIC_MESSAGE =
            "If an account exists for this email and it is unverified, we sent a new verification email.";
    private static final String FORGOT_PASSWORD_MESSAGE =
            "If an account exists for this email, we sent a password reset link.";

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenService tokenService;
    private final MailService mailService;
    private final CustomUserDetailsService userDetailsService;
    private final SessionRepository sessionRepository;
    private final SessionService sessionService;

    @Value("${session.inactivity-timeout}")
    private long sessionInactivityTimeoutMs;

    @Value("${session.absolute-lifetime}")
    private long sessionAbsoluteLifetimeMs;

    @Value("${auth.verification-token-expiration:86400000}")
    private long verificationTokenExpiration;

    @Value("${auth.password-reset-token-expiration:3600000}")
    private long passwordResetTokenExpiration;

    @Transactional
    public Map<String, String> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .build();

        userRepository.save(user);

        sendVerificationEmail(user);

        return Map.of("message", "Account created. Please verify your email before logging in.");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new EmailNotVerifiedException("Please verify your email before logging in.");
        }

        UUID sid = UUID.randomUUID();

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails, sid.toString());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails, sid.toString());

        sessionService.createSession(user, sessionAbsoluteLifetimeMs, "Web/PWA", sid,
                sessionService.hashVerifier(refreshToken));

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)
                || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        String email = jwtTokenProvider.extractEmail(refreshToken);
        String sid = jwtTokenProvider.extractSessionId(refreshToken);

        if (email == null || sid == null) {
            throw new InvalidRefreshTokenException();
        }

        UUID sidUuid;
        try {
            sidUuid = UUID.fromString(sid);
        } catch (IllegalArgumentException e) {
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new InvalidRefreshTokenException();
        }

        LocalDateTime now = LocalDateTime.now();

        Session session = sessionRepository.findWithUserBySid(sidUuid)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!session.getUser().getId().equals(user.getId())) {
            log.warn("Auth security event: refresh token user/session mismatch {}", sidUuid);
            throw new InvalidRefreshTokenException();
        }

        if (session.getRevokedAt() != null) {
            throw new InvalidRefreshTokenException();
        }

        if (now.isAfter(session.getAbsoluteExpirationAt())) {
            log.info("Auth: refresh rejected, absolute session expired for session {}", sidUuid);
            throw new InvalidRefreshTokenException();
        }

        if (now.isAfter(session.getLastActivityAt().plus(sessionInactivityTimeoutMs, ChronoUnit.MILLIS))) {
            log.info("Auth: refresh rejected, inactivity window elapsed for session {}", sidUuid);
            throw new InvalidRefreshTokenException();
        }

        String storedVerifier = session.getRefreshVerifierHash();
        if (!sessionService.verifierMatches(refreshToken, storedVerifier)) {
            sessionService.revoke(sidUuid, now);
            log.warn("Auth security event: refresh-token reuse detected, session revoked {}", sidUuid);
            throw new InvalidRefreshTokenException();
        }

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            throw new InvalidRefreshTokenException();
        }

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails, sid.toString());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails, sid.toString());

        int rotated = sessionRepository.rotateVerifier(
                sidUuid,
                storedVerifier,
                sessionService.hashVerifier(newRefreshToken),
                now);

        if (rotated == 0) {
            sessionService.revoke(sidUuid, now);
            log.warn("Auth security event: concurrent refresh-token reuse, session revoked {}", sidUuid);
            throw new InvalidRefreshTokenException();
        }

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    @Transactional
    public void logout(String refreshToken, String authorizationHeader) {
        String sid = sessionIdFromToken(refreshToken);
        if (sid != null) {
            sessionService.revoke(UUID.fromString(sid), LocalDateTime.now());
            return;
        }

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            sid = sessionIdFromToken(authorizationHeader.substring(7));
            if (sid != null) {
                sessionService.revoke(UUID.fromString(sid), LocalDateTime.now());
            }
        }
    }

    private String sessionIdFromToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String sid = jwtTokenProvider.extractSessionId(token);
        if (sid == null) {
            return null;
        }
        try {
            UUID.fromString(sid);
            return sid;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Transactional
    public Map<String, String> verifyEmail(String rawToken) {
        EmailVerificationToken token = verificationTokenRepository
                .findByTokenHash(tokenService.hash(rawToken))
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification link"));

        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired verification link");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        token.setUsedAt(LocalDateTime.now());
        verificationTokenRepository.save(token);

        mailService.sendWelcomeEmail(user.getName(), user.getEmail());

        return Map.of("message", "Email verified successfully. You can now log in.");
    }

    @Transactional
    public Map<String, String> resendVerification(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null && !Boolean.TRUE.equals(user.getEmailVerified())) {
            verificationTokenRepository.deleteByUserId(user.getId());
            sendVerificationEmail(user);
        }

        return Map.of("message", RESEND_GENERIC_MESSAGE);
    }

    @Transactional
    public Map<String, String> forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUserId(user.getId());

            String rawToken = tokenService.generateToken();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(tokenService.hash(rawToken))
                    .expiresAt(LocalDateTime.now().plus(passwordResetTokenExpiration, java.time.temporal.ChronoUnit.MILLIS))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            mailService.sendPasswordResetEmail(user.getName(), user.getEmail(), rawToken);
        });

        return Map.of("message", FORGOT_PASSWORD_MESSAGE);
    }

    @Transactional
    public Map<String, String> resetPassword(String rawToken, String newPassword, String confirmNewPassword) {
        if (!newPassword.equals(confirmNewPassword)) {
            throw new RuntimeException("Passwords do not match");
        }

        PasswordResetToken token = passwordResetTokenRepository
                .findByTokenHash(tokenService.hash(rawToken))
                .orElseThrow(() -> new RuntimeException(INVALID_RESET_LINK));

        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException(INVALID_RESET_LINK);
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);

        return Map.of("message", "Your password has been reset successfully.");
    }

    private void sendVerificationEmail(User user) {
        String rawToken = tokenService.generateToken();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .tokenHash(tokenService.hash(rawToken))
                .expiresAt(LocalDateTime.now().plus(verificationTokenExpiration, java.time.temporal.ChronoUnit.MILLIS))
                .build();
        verificationTokenRepository.save(verificationToken);

        mailService.sendVerificationEmail(user.getName(), user.getEmail(), rawToken);
    }
}