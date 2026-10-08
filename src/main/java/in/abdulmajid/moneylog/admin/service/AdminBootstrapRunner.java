package in.abdulmajid.moneylog.admin.service;

import in.abdulmajid.moneylog.admin.model.Admin;
import in.abdulmajid.moneylog.admin.model.AdminRole;
import in.abdulmajid.moneylog.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

/**
 * Creates the first OWNER admin from environment configuration. Password values
 * are never logged. If the configured password no longer matches the stored
 * hash, the hash is rotated to the configured value.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.bootstrap.email:}")
    private String bootstrapEmail;

    @Value("${admin.bootstrap.password:}")
    private String bootstrapPassword;

    @Value("${admin.bootstrap.name:Admin}")
    private String bootstrapName;

    @Override
    public void run(ApplicationArguments args) {
        String email = normalize(bootstrapEmail);
        String password = bootstrapPassword == null ? "" : bootstrapPassword;

        if (email.isEmpty() || password.isEmpty()) {
            log.info("Admin bootstrap skipped: no ADMIN_EMAIL/ADMIN_PASSWORD configured");
            return;
        }

        Optional<Admin> existing = adminRepository.findByEmail(email);
        if (existing.isEmpty()) {
            Admin admin = Admin.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .name(bootstrapName == null || bootstrapName.isBlank() ? "Admin" : bootstrapName)
                    .role(AdminRole.OWNER)
                    .active(true)
                    .build();
            adminRepository.save(admin);
            log.info("Admin bootstrap: created OWNER account for {}", email);
            return;
        }

        Admin admin = existing.get();
        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            admin.setPasswordHash(passwordEncoder.encode(password));
            adminRepository.save(admin);
            log.info("Admin bootstrap: rotated stored password for {}", email);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
