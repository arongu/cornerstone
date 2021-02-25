package cornerstone.webapp.security;

import cornerstone.webapp.services.accounts.management.UserRole;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

public class UserPrincipal implements Principal {
    private final String email;
    private final Set<UserRole> userRoles;

    public UserPrincipal(final String email, final Set<UserRole> userRoles) {
        this.email = email;
        this.userRoles = Collections.unmodifiableSet(userRoles);
    }

    @Override
    public String getName() {
        return email;
    }

    public Set<UserRole> getUserRoles() {
        return userRoles;
    }
}
