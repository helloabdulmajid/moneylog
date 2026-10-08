package in.abdulmajid.moneylog.featureflag.model;

import in.abdulmajid.moneylog.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feature_flags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureFlag extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String key;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    @Column(name = "updated_by", length = 254)
    private String updatedBy;
}
