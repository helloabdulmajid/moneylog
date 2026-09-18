package in.abdulmajid.moneylog.user.controller;

import in.abdulmajid.moneylog.common.CurrentUserHelper;
import in.abdulmajid.moneylog.user.dto.request.ChangePasswordRequest;
import in.abdulmajid.moneylog.user.dto.request.UpdatePreferencesRequest;
import in.abdulmajid.moneylog.user.dto.request.UpdateProfileRequest;
import in.abdulmajid.moneylog.user.dto.response.UserPreferenceResponse;
import in.abdulmajid.moneylog.user.dto.response.UserProfileResponse;
import in.abdulmajid.moneylog.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUserHelper currentUserHelper;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile() {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @GetMapping("/preferences")
    public ResponseEntity<UserPreferenceResponse> getPreferences() {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(userService.getPreferences(userId));
    }

    @PutMapping("/preferences")
    public ResponseEntity<UserPreferenceResponse> updatePreferences(
            @Valid @RequestBody UpdatePreferencesRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(userService.updatePreferences(userId, request));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        userService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount() {
        UUID userId = currentUserHelper.getCurrentUserId();
        userService.deleteAccount(userId);
        return ResponseEntity.noContent().build();
    }
}
