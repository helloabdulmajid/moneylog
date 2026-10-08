package in.abdulmajid.moneylog.featureflag.controller;

import in.abdulmajid.moneylog.admin.service.AdminFeatureFlagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public, unauthenticated view of enabled feature flags only. Disabled flags
 * and admin metadata are never exposed.
 */
@RestController
@RequestMapping("/feature-flags")
@RequiredArgsConstructor
public class FeatureFlagController {

    private final AdminFeatureFlagService adminFeatureFlagService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> enabledFlags() {
        return ResponseEntity.ok(adminFeatureFlagService.publicEnabledKeys());
    }
}
