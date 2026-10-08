package in.abdulmajid.moneylog.admin.model;

public enum AdminPermission {

    DASHBOARD_VIEW("dashboard:view"),
    FEEDBACK_READ("feedback:read"),
    FEEDBACK_WRITE("feedback:write"),
    USERS_READ("users:read"),
    USERS_SESSIONS_REVOKE("users:sessions:revoke"),
    USERS_VERIFY("users:verify"),
    FLAGS_READ("flags:read"),
    FLAGS_WRITE("flags:write"),
    AUDIT_READ("audit:read"),
    ADMINS_MANAGE("admins:manage");

    private final String code;

    AdminPermission(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public String authority() {
        return "admin:" + code;
    }
}
