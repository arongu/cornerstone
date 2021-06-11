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
    private final JwtParser jwtParser;

    private static Set<UserRole> extractUserRoles(final Claims claims) {
        final String role = claims.get(JWT_SERVICE_CLAIMS.role.name()).toString();
        final Set<UserRole> userRoles = new HashSet<>();

        try {
            final UserRole userRole = UserRole.valueOf(role);
            userRoles.add(userRole);

        } catch (final IllegalArgumentException | NullPointerException e) {
            logger.error(String.format(INVALID_USER_ROLE_FOR_SUBJECT, role, claims.getSubject()));
        }

        return userRoles;
    }

    @Inject
    public AuthenticationFilter(final SigningKeyResolver signingKeyResolver) {
        this.jwtParser = Jwts.parserBuilder().setSigningKeyResolver(signingKeyResolver).build();
    }

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) throws IOException {
        final String authorizationHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        final boolean secure             = containerRequestContext.getSecurityContext().isSecure();

        Principal principal;
        Claims claims;
        Set<UserRole> userRoles;

        if ( authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            try {
                final String jws = authorizationHeader.substring(BEARER.length());
                claims           = jwtParser.parseClaimsJwt(jws).getBody();
                principal        = new PrincipalImpl(claims.getSubject());
                userRoles        = extractUserRoles(claims);

            } catch (final Exception e) {
                claims    = null;
                principal = null;
                userRoles = null;
            }

        } else {
            claims    = null;
            principal = null;
            userRoles = null;
        }

        final SecurityContext securityContext = new JwtSecurityContext(secure, principal, userRoles, claims);
        containerRequestContext.setSecurityContext(securityContext);
    }
}
