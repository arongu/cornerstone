package cornerstone.webapp.security.filters;

import cornerstone.webapp.security.JwtSecurityContext;
import cornerstone.webapp.security.PrincipalImpl;
import cornerstone.webapp.services.accounts.management.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolver;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

class AuthenticationFilter implements ContainerRequestFilter {
    private static final String BEARER = "Bearer ";
    private static final String ROLE   = "role"; // should be moved to enum so JWTCreatorService and this can verify
                                                 // both service use the same "words"

    private final JwtParser jwtParser;
    private SecurityContext securityContext;

    private static Set<UserRole> extractUserRoles(final Claims claims) {
        final String role = claims.get(ROLE).toString();
        final Set<UserRole> userRoles = new HashSet<>();

        try {
            final UserRole userRole = UserRole.valueOf(role);
            userRoles.add(userRole);

        } catch (final IllegalArgumentException | NullPointerException e) {
            // add log for failed parse? ERROR level most likely
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
