package cornerstone.webapp.security;

import cornerstone.webapp.services.accounts.management.UserRole;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public class TokenSecurityContext implements SecurityContext {
    private final UserPrincipal userPrincipal;
    private final boolean secure;

    public TokenSecurityContext(final UserPrincipal userPrincipal, final boolean secure) {
        this.userPrincipal = userPrincipal;
        this.secure = secure;
    }

    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    @Override
    public boolean isUserInRole(final String s) {
        return userPrincipal.getUserRoles().contains(UserRole.valueOf(s));
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
