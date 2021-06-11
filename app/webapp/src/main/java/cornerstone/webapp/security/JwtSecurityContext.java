package cornerstone.webapp.security;

import cornerstone.webapp.services.accounts.management.UserRole;
import io.jsonwebtoken.Claims;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Collections;
import java.util.Set;

public class JwtSecurityContext implements SecurityContext {
    private final Principal userPrincipal;
    private final Set<UserRole> userRoles;
    private final Claims claims;
    private final boolean secure;

    public JwtSecurityContext(final boolean secure,
                              final Principal userPrincipal,
                              final Set<UserRole> userRoles,
                              final Claims claims) {

        this.claims        = claims;
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
        if ( userRoles != null ) {
            return userRoles.contains(UserRole.valueOf(s));
        }

        return false;
    }

    @Override
    public boolean isSecure() {
        return this.secure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }

    public Claims getClaims() {
        return claims;
    }
}
