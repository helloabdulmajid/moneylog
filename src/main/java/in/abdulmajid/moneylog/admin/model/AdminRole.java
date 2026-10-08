package in.abdulmajid.moneylog.admin.model;

import java.util.EnumSet;
import java.util.Set;

import static in.abdulmajid.moneylog.admin.model.AdminPermission.*;

public enum AdminRole {

    OWNER(EnumSet.allOf(AdminPermission.class)),
    ADMIN(EnumSet.of(
            DASHBOARD_VIEW,
            FEEDBACK_READ,
            FEEDBACK_WRITE,
            USERS_READ,
            USERS_SESSIONS_REVOKE,
            USERS_VERIFY,
            FLAGS_READ,
            FLAGS_WRITE,
            AUDIT_READ)),
    SUPPORT(EnumSet.of(
            DASHBOARD_VIEW,
            FEEDBACK_READ,
            FEEDBACK_WRITE,
            USERS_READ,
            AUDIT_READ));

    private final Set<AdminPermission> permissions;

    AdminRole(Set<AdminPermission> permissions) {
        this.permissions = permissions;
    }

    public Set<AdminPermission> permissions() {
        return permissions;
    }
}
