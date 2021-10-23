package cornerstone.webapp.security;

import cornerstone.webapp.services.accounts.management.enums.SYSTEM_ROLE_ENUM;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Collections;
import java.util.Set;


// TODO probably need to add the methods which is allowed,
// NO_ROLE also has to be able to call the get methods and such
// Figure out how to call method filter
public class JwtSecurityContext implements SecurityContext {
    private final Principal userPrincipal;
    private final Set<SYSTEM_ROLE_ENUM> roles;
    private final Claims claims;
    private final boolean secure;

    private static final Logger logger = LoggerFactory.getLogger(JwtSecurityContext.class);

    public JwtSecurityContext(final boolean secure,
                              final Principal userPrincipal,
                              final Set<SYSTEM_ROLE_ENUM> roles,
                              final Claims claims) {

        this.claims        = claims;
        this.userPrincipal = userPrincipal;
        this.roles = roles != null ? Collections.unmodifiableSet(roles) : null;
        this.secure        = secure;
    }

    @Override
    public Principal getUserPrincipal() {
        logger.info("getUserPrincipal()");
        return userPrincipal;
    }

    @Override
    public boolean isUserInRole(final String s) {
        logger.info("isUserInRole(final String s) {}}", s);
        if ( roles != null ) {
            return roles.contains(SYSTEM_ROLE_ENUM.valueOf(s));
        }

        return false;
    }

    @Override
    public boolean isSecure() {
        logger.info("isSecure()");
        return this.secure;
    }

    @Override
    public String getAuthenticationScheme() {
        logger.info("getAuthenticationScheme()");
        return "Bearer";
    }

    public Claims getClaims() {
        logger.info("getClaims()");
        return claims;
    }
}
