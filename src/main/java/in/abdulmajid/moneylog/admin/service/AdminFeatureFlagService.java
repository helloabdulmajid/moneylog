package in.abdulmajid.moneylog.admin.service;

import in.abdulmajid.moneylog.admin.dto.request.AdminCreateFlagRequest;
import in.abdulmajid.moneylog.admin.dto.request.AdminUpdateFlagRequest;
import in.abdulmajid.moneylog.admin.dto.response.FeatureFlagResponse;
import in.abdulmajid.moneylog.admin.security.AdminContextHelper;
import in.abdulmajid.moneylog.common.ResourceNotFoundException;
import in.abdulmajid.moneylog.featureflag.model.FeatureFlag;
import in.abdulmajid.moneylog.featureflag.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminFeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;
    private final AuditService auditService;
    private final AdminContextHelper adminContextHelper;

    @Transactional(readOnly = true)
    public List<FeatureFlagResponse> list() {
        return featureFlagRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(FeatureFlag::getKey))
                .map(AdminFeatureFlagService::toDto)
                .toList();
    }

    @Transactional
    public FeatureFlagResponse create(AdminCreateFlagRequest request) {
        String key = request.getKey().trim();
        if (featureFlagRepository.findByKey(key).isPresent()) {
            throw new IllegalArgumentException("A flag with this key already exists");
        }

        FeatureFlag flag = FeatureFlag.builder()
                .key(key)
                .description(request.getDescription())
                .enabled(Boolean.TRUE.equals(request.getEnabled()))
                .updatedBy(adminContextHelper.getCurrentAdmin().getEmail())
                .build();
        featureFlagRepository.saveAndFlush(flag);

        auditService.record("feature_flag.created", "feature_flag", flag.getId().toString(),
                "key=" + flag.getKey());

        return toDto(flag);
    }

    @Transactional
    public FeatureFlagResponse update(UUID id, AdminUpdateFlagRequest request) {
        FeatureFlag flag = findOrThrow(id);

        if (request.getDescription() != null) {
            flag.setDescription(request.getDescription());
        }
        if (request.getEnabled() != null) {
            flag.setEnabled(request.getEnabled());
        }
        flag.setUpdatedBy(adminContextHelper.getCurrentAdmin().getEmail());
        featureFlagRepository.saveAndFlush(flag);

        auditService.record("feature_flag.updated", "feature_flag", flag.getId().toString(),
                "key=" + flag.getKey() + " enabled=" + flag.getEnabled());

        return toDto(flag);
    }

    @Transactional
    public void delete(UUID id) {
        FeatureFlag flag = findOrThrow(id);
        featureFlagRepository.delete(flag);
        auditService.record("feature_flag.deleted", "feature_flag", id.toString(),
                "key=" + flag.getKey());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> publicEnabledKeys() {
        List<String> keys = featureFlagRepository.findByEnabledTrue().stream()
                .map(FeatureFlag::getKey)
                .sorted()
                .toList();
        return Map.of("enabled", keys);
    }

    private FeatureFlag findOrThrow(UUID id) {
        return featureFlagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature flag not found"));
    }

    static FeatureFlagResponse toDto(FeatureFlag flag) {
        return FeatureFlagResponse.builder()
                .id(flag.getId())
                .key(flag.getKey())
                .description(flag.getDescription())
                .enabled(flag.getEnabled())
                .updatedBy(flag.getUpdatedBy())
                .createdAt(flag.getCreatedAt())
                .updatedAt(flag.getUpdatedAt())
                .build();
    }
}
