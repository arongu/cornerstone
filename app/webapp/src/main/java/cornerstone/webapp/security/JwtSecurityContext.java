package cornerstone.webapp.security;

import cornerstone.webapp.services.accounts.management.UserRole;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Collections;
import java.util.Set;

public class JwtSecurityContext implements SecurityContext {
    private final PrincipalImpl userPrincipal;
    private final Set<UserRole> userRoles;
    private final boolean secure;

    public JwtSecurityContext(final PrincipalImpl userPrincipal, final boolean secure, final Set<UserRole> userRoles) {
        this.userPrincipal = userPrincipal;
        this.userRoles     = Collections.unmodifiableSet(userRoles);
        this.secure        = secure;
    }

    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    @Override
    public boolean isUserInRole(final String s) {
        return userRoles.contains(UserRole.valueOf(s));
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}
