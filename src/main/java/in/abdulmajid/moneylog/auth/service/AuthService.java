package in.abdulmajid.moneylog.auth.service;

import in.abdulmajid.moneylog.auth.dto.request.LoginRequest;
import in.abdulmajid.moneylog.auth.dto.request.RegisterRequest;
import in.abdulmajid.moneylog.auth.dto.response.AuthResponse;
import in.abdulmajid.moneylog.auth.exception.EmailNotVerifiedException;
import in.abdulmajid.moneylog.auth.model.EmailVerificationToken;
import in.abdulmajid.moneylog.auth.model.PasswordResetToken;
import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.auth.repository.EmailVerificationTokenRepository;
import in.abdulmajid.moneylog.auth.repository.PasswordResetTokenRepository;
import in.abdulmajid.moneylog.auth.repository.UserRepository;
import in.abdulmajid.moneylog.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
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

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new EmailNotVerifiedException("Please verify your email before logging in.");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .email(user.getEmail())
                .name(user.getName())
                .build();
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