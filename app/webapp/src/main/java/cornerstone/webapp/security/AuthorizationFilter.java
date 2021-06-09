package cornerstone.webapp.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) throws RuntimeException {
        final Class<?> resourceClass = resourceInfo.getResourceClass();
        final Method resourceMethod  = resourceInfo.getResourceMethod();

        // Deny if no class or method level annotations are present.
        if ( resourceClass.getAnnotations().length == 0 && resourceMethod.getAnnotations().length == 0) {
            throw new ForbiddenException("No annotation present on class or methods, nothing to work with.");
        }

        // -- Class ---------------------------------------------------------------------------------------------
        if ( resourceClass.isAnnotationPresent(DenyAll.class)) {
            throw new ForbiddenException("@DenyAll on class.");
        }

        if (resourceClass.isAnnotationPresent(RolesAllowed.class)) {
            final String[] classLevelRolesAllowed = resourceClass.getAnnotation(RolesAllowed.class).value();

            if ( classLevelRolesAllowed.length > 0) {
                final SecurityContext secContext = containerRequestContext.getSecurityContext();

                for ( final String r : classLevelRolesAllowed) {
                    if ( secContext.isUserInRole(r)) {
                        return;
                    }
                }
            }

            throw new ForbiddenException("@RolesAllowed on class, but user is not in the allowed roles.");
        }

        if ( resourceClass.isAnnotationPresent(PermitAll.class)){
            return;
        }

        // -- Method ---------------------------------------------------------------------------------------------
        if (resourceMethod.getAnnotations().length == 0) {
            throw new ForbiddenException("All class level annotations exhausted, and no method level annotations present.");
        }

        if ( resourceMethod.isAnnotationPresent(DenyAll.class)) {
            throw new ForbiddenException("@DenyAll on method.");
        }

        if ( resourceMethod.isAnnotationPresent(RolesAllowed.class)) {
            final String[] methodLevelRolesAllowed = resourceMethod.getAnnotation(RolesAllowed.class).value();

            if ( methodLevelRolesAllowed.length > 0) {
                final SecurityContext secContext = containerRequestContext.getSecurityContext();

                for ( final String r : methodLevelRolesAllowed) {
                    if ( secContext.isUserInRole(r)) {
                        return;
                    }
                }
            }

            throw new ForbiddenException("@RolesAllowed on method, but user is not in the allowed roles.");
        }

        if ( resourceMethod.isAnnotationPresent(PermitAll.class)){
            return;
        }

        throw new ForbiddenException("All method and class level rules are exhausted, denying by default.");
    }
}
