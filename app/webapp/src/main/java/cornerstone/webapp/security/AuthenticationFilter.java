package cornerstone.webapp.security;

import cornerstone.webapp.services.accounts.management.UserRole;
import cornerstone.webapp.services.jwt.JWT_SERVICE_CLAIMS;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

@Provider
@Priority(Priorities.AUTHENTICATION)
class AuthenticationFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String BEARER = "Bearer ";
    private static final String INVALID_USER_ROLE_FOR_SUBJECT = "Invalid user role detected in a signed JWT! role: '%s', subject: '%s'!";

    // TODO this is not a single string, this is a set ! (for future, multiple roles will be available)
    // SERIOUS issue
    private static Set<UserRole> extractUserRoles(final Claims claims) {
        final Set<UserRole> userRoles = new HashSet<>();
        String strRole = "default";

        try {
            strRole                 = claims.get(JWT_SERVICE_CLAIMS.role.name()).toString();
            final UserRole userRole = UserRole.valueOf(strRole);
            userRoles.add(userRole);

        } catch (final IllegalArgumentException | NullPointerException e) {
            logger.error(String.format(INVALID_USER_ROLE_FOR_SUBJECT, strRole, claims.getSubject()));
        }

        return userRoles;
    }

    @Inject
    private final SigningKeyResolver signingKeyResolver;

    public AuthenticationFilter(final SigningKeyResolver signingKeyResolver) {
        this.signingKeyResolver = signingKeyResolver;
    }

    //secure, principal, userRoles, claims
    @Override
    public void filter(final ContainerRequestContext containerRequestContext) throws IOException {
        logger.info("............................ AUT ATU ATUAUTAu");

        final String authorizationHeader;
        // security context elements:
        boolean secure;
        final Principal principal;
        final Set<UserRole> userRoles;
        final Claims claims;

        if ( containerRequestContext == null ) {
            throw new IOException("containerRequestContext is null!");
        }

        authorizationHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        SecurityContext sc  = containerRequestContext.getSecurityContext();
        secure              = sc != null && sc.isSecure();

        if ( authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            try {
                final String jws  = authorizationHeader.substring(BEARER.length());
                final JwtParser p = Jwts.parserBuilder().setSigningKeyResolver(signingKeyResolver).build();

                claims    = p.parseClaimsJwt(jws).getBody();
                principal = new PrincipalImpl(claims.getSubject());
                userRoles = extractUserRoles(claims);

            } catch (final Exception e) {
                throw new IOException(e.getMessage());
            }

        } else {
            principal = new PrincipalImpl("Anonymous");
            claims    = null;
            userRoles = new HashSet<>();
            userRoles.add(UserRole.NO_ROLE);
        }

        containerRequestContext.setSecurityContext(
                new JwtSecurityContext(secure, principal, userRoles, claims)
        );
    }
}
