package cornerstone.webapp.security;

import cornerstone.webapp.services.accounts.management.UserRole;
import cornerstone.webapp.services.jwt.JWT_SERVICE_CLAIMS;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String BEARER = "Bearer ";
    private static final String INVALID_USER_ROLE_FOR_SUBJECT = "Invalid user role detected in a signed JWT! subject: '%s', role: '%s' !";

    // TODO this is not a single string, this is a set ! (for future, multiple roles will be available) // SERIOUS issue
    private static Set<UserRole> extractUserRoles(final Claims claims) {
        Set<UserRole> userRoles = null;
        String roleFromClaims   = "UNSET";

        try {
            roleFromClaims    = claims.get(JWT_SERVICE_CLAIMS.role.name()).toString();
            UserRole userRole = UserRole.valueOf(roleFromClaims);
            userRoles         = new HashSet<>();
            userRoles.add(userRole);

        } catch (final IllegalArgumentException | NullPointerException e) {
            logger.error(SecurityMessageElements.SECURITY_PREFIX + String.format(INVALID_USER_ROLE_FOR_SUBJECT, roleFromClaims, claims.getSubject()));
        }

        return userRoles;
    }

    @Context
    private HttpServletRequest httpServletRequest;

    @Inject
    private SigningKeyResolver signingKeyResolver;

    public AuthenticationFilter() {
    }

    public AuthenticationFilter(final SigningKeyResolver signingKeyResolver) {
        this.signingKeyResolver = signingKeyResolver;
    }

    //secure, principal, userRoles, claims
    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException, ForbiddenException {
        final String m = String.format(SecurityMessageElements.SECURITY_PREFIX + "Filtering URI='%s', METHOD='%s', ADDR='%s'", httpServletRequest.getRequestURI(), httpServletRequest.getMethod(), httpServletRequest.getLocalAddr());
        logger.info(m);

        if ( containerRequestContext == null) {
            final String msg = SecurityMessageElements.SECURITY_PREFIX + "containerRequestContext is null!";
            logger.error(msg);
            throw new IOException();
        }

        if ( containerRequestContext.getSecurityContext() == null) {
            final String msg = SecurityMessageElements.SECURITY_PREFIX + "containerRequestContext.getSecurityContext() returned null!";
            logger.error(msg);
            throw new IOException();
        }

        final String authorizationHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        boolean secure                   = containerRequestContext.getSecurityContext().isSecure();
        String subject                   = null;
        Set<UserRole> userRoles          = null;
        Claims claims                    = null;


        if ( authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            final String    jws       = authorizationHeader.substring(BEARER.length());
            final JwtParser jwtParser = Jwts.parserBuilder().setSigningKeyResolver(signingKeyResolver).build();

            Jws<Claims> claimsJws = null;
            try {
                claimsJws = jwtParser.parseClaimsJws(jws);

            } catch (final ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException | NoSuchElementException exception) {
                logger.info(SecurityMessageElements.SECURITY_PREFIX + "JWS could not be parsed! Anonymous level permissions granted.");

            } catch (final NullPointerException nullPointerException) {
                throw new ForbiddenException();
            }

            if ( claimsJws != null && claimsJws.getBody() != null) {
                claims    = claimsJws.getBody();
                userRoles = extractUserRoles(claims);
                subject   = claims.getSubject();
            }
        }

        final Principal principal = subject == null ? new PrincipalImpl("Anonymous") : new PrincipalImpl(subject);
        containerRequestContext.setSecurityContext(
                new JwtSecurityContext(secure, principal, userRoles, claims)
        );
    }
}
