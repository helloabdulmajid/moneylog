package in.abdulmajid.moneylog.user.service;

import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.auth.repository.EmailVerificationTokenRepository;
import in.abdulmajid.moneylog.auth.repository.PasswordResetTokenRepository;
import in.abdulmajid.moneylog.auth.repository.UserRepository;
import in.abdulmajid.moneylog.category.repository.CategoryRepository;
import in.abdulmajid.moneylog.category.repository.SubcategoryRepository;
import in.abdulmajid.moneylog.expense.repository.ExpenseRepository;
import in.abdulmajid.moneylog.payment.repository.BillPaymentRepository;
import in.abdulmajid.moneylog.payment.repository.CreditCardRepository;
import in.abdulmajid.moneylog.payment.repository.PaymentAppRepository;
import in.abdulmajid.moneylog.payment.repository.PaymentSourceRepository;
import in.abdulmajid.moneylog.user.dto.request.ChangePasswordRequest;
import in.abdulmajid.moneylog.user.dto.request.UpdatePreferencesRequest;
import in.abdulmajid.moneylog.user.dto.request.UpdateProfileRequest;
import in.abdulmajid.moneylog.user.dto.response.UserPreferenceResponse;
import in.abdulmajid.moneylog.user.dto.response.UserProfileResponse;
import in.abdulmajid.moneylog.user.model.UserPreference;
import in.abdulmajid.moneylog.user.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final PasswordEncoder passwordEncoder;

    private final ExpenseRepository expenseRepository;
    private final BillPaymentRepository billPaymentRepository;
    private final CreditCardRepository creditCardRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final CategoryRepository categoryRepository;
    private final PaymentAppRepository paymentAppRepository;
    private final PaymentSourceRepository paymentSourceRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public UserProfileResponse getProfile(UUID userId) {
        return toProfileResponse(getUser(userId));
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = getUser(userId);
        user.setName(request.getName().trim());
        user.setGender(request.getGender());
        return toProfileResponse(userRepository.save(user));
    }

    public UserPreferenceResponse getPreferences(UUID userId) {
        return toPreferenceResponse(getOrCreatePreferences(userId));
    }

    @Transactional
    public UserPreferenceResponse updatePreferences(UUID userId, UpdatePreferencesRequest request) {
        UserPreference preferences = getOrCreatePreferences(userId);

        if (request.getTheme() != null) preferences.setTheme(request.getTheme());
        if (request.getCurrency() != null) preferences.setCurrency(request.getCurrency().trim().toUpperCase());
        if (request.getTimezone() != null) preferences.setTimezone(request.getTimezone().trim());
        if (request.getDateFormat() != null) preferences.setDateFormat(request.getDateFormat());
        if (request.getTimeFormat() != null) preferences.setTimeFormat(request.getTimeFormat());
        if (request.getLanguage() != null) preferences.setLanguage(request.getLanguage().trim());
        if (request.getBillReminderEnabled() != null) preferences.setBillReminderEnabled(request.getBillReminderEnabled());
        if (request.getMismatchAlertEnabled() != null) preferences.setMismatchAlertEnabled(request.getMismatchAlertEnabled());
        if (request.getSpendingSummaryEnabled() != null) preferences.setSpendingSummaryEnabled(request.getSpendingSummaryEnabled());
        if (request.getReminderDaysBefore() != null) preferences.setReminderDaysBefore(request.getReminderDaysBefore());

        return toPreferenceResponse(userPreferenceRepository.save(preferences));
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = getUser(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new RuntimeException("New password must be different from the current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(UUID userId) {
        User user = getUser(userId);

        billPaymentRepository.deleteByUserId(userId);
        expenseRepository.deleteByUserId(userId);
        creditCardRepository.deleteByUserId(userId);
        subcategoryRepository.deleteByUserId(userId);
        categoryRepository.deleteByUserId(userId);
        paymentAppRepository.deleteByUserId(userId);
        paymentSourceRepository.deleteByUserId(userId);
        userPreferenceRepository.deleteByUserId(userId);
        emailVerificationTokenRepository.deleteByUserId(userId);
        passwordResetTokenRepository.deleteByUserId(userId);

        userRepository.delete(user);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private UserPreference getOrCreatePreferences(UUID userId) {
        return userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> userPreferenceRepository.save(
                        UserPreference.builder().user(getUser(userId)).build()));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .gender(user.getGender())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private UserPreferenceResponse toPreferenceResponse(UserPreference preferences) {
        return UserPreferenceResponse.builder()
                .theme(preferences.getTheme())
                .currency(preferences.getCurrency())
                .timezone(preferences.getTimezone())
                .dateFormat(preferences.getDateFormat())
                .timeFormat(preferences.getTimeFormat())
                .language(preferences.getLanguage())
                .billReminderEnabled(preferences.getBillReminderEnabled())
                .mismatchAlertEnabled(preferences.getMismatchAlertEnabled())
                .spendingSummaryEnabled(preferences.getSpendingSummaryEnabled())
                .reminderDaysBefore(preferences.getReminderDaysBefore())
                .build();
    }
}
