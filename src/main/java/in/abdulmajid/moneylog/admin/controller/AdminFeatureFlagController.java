package in.abdulmajid.moneylog.admin.controller;

import in.abdulmajid.moneylog.admin.dto.request.AdminCreateFlagRequest;
import in.abdulmajid.moneylog.admin.dto.request.AdminUpdateFlagRequest;
import in.abdulmajid.moneylog.admin.dto.response.FeatureFlagResponse;
import in.abdulmajid.moneylog.admin.service.AdminFeatureFlagService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Hidden
@RestController
@RequestMapping("/admin/feature-flags")
@RequiredArgsConstructor
public class AdminFeatureFlagController {

    private final AdminFeatureFlagService adminFeatureFlagService;

    @GetMapping
    @PreAuthorize("hasAuthority('admin:flags:read')")
    public ResponseEntity<List<FeatureFlagResponse>> list() {
        return ResponseEntity.ok(adminFeatureFlagService.list());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin:flags:write')")
    public ResponseEntity<FeatureFlagResponse> create(@Valid @RequestBody AdminCreateFlagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminFeatureFlagService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:flags:write')")
    public ResponseEntity<FeatureFlagResponse> update(@PathVariable UUID id,
                                                      @Valid @RequestBody AdminUpdateFlagRequest request) {
        return ResponseEntity.ok(adminFeatureFlagService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:flags:write')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        adminFeatureFlagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
