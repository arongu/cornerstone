package cornerstone.webapp.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.AccessDeniedException;

//@Provider
public class AuthorizationFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    public AuthorizationFilter() {
        logger.info("CTOR: AuthorizationFilter() AuthorizationFilter() AuthorizationFilter()AuthorizationFilter() AuthorizationFilter()");
    }

    /**
     * Perform authorization based on roles.
     *
     * @param rolesAllowed Array of allowed roles.
     * @param requestContext Context of the request.
     */
    private void performAuthorization(final String[] rolesAllowed, final ContainerRequestContext requestContext) throws AccessDeniedException {
        if ( rolesAllowed != null && requestContext != null ) {
            // this is where the JWS should be checked for its roles
            // and maybe prior to this
            // Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jws).getBody().getSubject()
//            if ( rolesAllowed.length > 0 && !isAuthenticated(requestContext)) {
//                throw new AccessDeniedException("Authentication is required to perform this action.");
//            }
//
//            for (final String role : rolesAllowed) {
//                if (requestContext.getSecurityContext().isUserInRole(role)) {
//                    return;
//                }
//            }
        }

        throw new AccessDeniedException("You don't have permissions to perform this action.");
    }

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) throws IOException {
        final Method method = resourceInfo.getResourceMethod();
        final SecurityContext securityContext = containerRequestContext.getSecurityContext();
        logger.info("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");

        logger.info("securityContext.getUserPrincipal(): " + securityContext.getUserPrincipal());
        logger.info("securityContext.isSecure(): " + securityContext.isSecure());

//        // 0
//        if ( method.isAnnotationPresent(DenyAll.class))  {
//            throw new AccessDeniedException("DenyAll");
//        }
//
//        // 1
//        final RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
//        if (rolesAllowed != null) {
//            p
//        }
//
//        //
//        if ( method.isAnnotationPresent(PermitAll.class)) {
//            return;
//        }

    }
}
