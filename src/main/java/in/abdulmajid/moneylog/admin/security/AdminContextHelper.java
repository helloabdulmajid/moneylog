package in.abdulmajid.moneylog.admin.security;

import in.abdulmajid.moneylog.admin.model.Admin;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AdminContextHelper {

    public Admin getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Admin admin)) {
            throw new IllegalStateException("No authenticated admin");
        }
        return admin;
    }
}
