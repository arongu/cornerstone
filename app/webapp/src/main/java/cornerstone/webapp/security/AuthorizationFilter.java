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

    // Early
    private static final String NO_ANNOTATIONS             = "No annotation present on class or methods, nothing to work with. (Denied)" ;
    // Class
    private static final String CLASS_DENY_ALL             = "@DenyAll on class. (Denied)" ;
    private static final String CLASS_ROLES_ALLOWED_DENY   = "@RolesAllowed on class, but user is not in the allowed roles. (Denied)" ;
    private static final String CLASS_ROLES_ALLOWED_ALLOW  = "@RolesAllowed, user is in the allowed roles. (Allowed)" ;
    private static final String CLASS_PERMIT_ALL           = "@PermitAll on class. (Allowed)" ;
    // Method
    private static final String METHOD_DENY_ALL            = "@DenyAll on method. (Denied)" ;
    private static final String METHOD_ROLES_ALLOWED_DENY  = "@RolesAllowed on method, but user is not in the allowed roles. (Denied)" ;
    private static final String METHOD_ROLES_ALLOWED_ALLOW = "@RolesAllowed, user is in the allowed roles. (Allowed)" ;
    private static final String METHOD_PERMIT_ALL          = "@PermitAll on class. (Allowed)" ;
    // Default
    private static final String ALL_EXHAUSTED_DENY         = "All method and class level rules are exhausted, denying by default. (Denied)" ;

    private static boolean isThereAnAllowedRoleInSecurityContext(final String[] rolesFromAnnotation, final SecurityContext securityContext) {
        if ( rolesFromAnnotation != null && rolesFromAnnotation.length > 0) {
            for ( final String r : rolesFromAnnotation) {
                if ( securityContext.isUserInRole(r)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) throws RuntimeException {
        final Class<?> resourceClass = resourceInfo.getResourceClass();
        final Method resourceMethod  = resourceInfo.getResourceMethod();

        // -- Early deny, if no annotation is present.
        if ( resourceClass.getAnnotations().length == 0 && resourceMethod.getAnnotations().length == 0) {
            logger.info(NO_ANNOTATIONS);
            throw new ForbiddenException(NO_ANNOTATIONS);
        }

        // -- Class
        if ( resourceClass.isAnnotationPresent(DenyAll.class)) {
            logger.info(CLASS_DENY_ALL);
            throw new ForbiddenException(CLASS_DENY_ALL);
        }

        if ( resourceClass.isAnnotationPresent(RolesAllowed.class)) {
            final String[] rolesAllowedOnClass = resourceClass.getAnnotation(RolesAllowed.class).value();
            final SecurityContext secContext   = containerRequestContext.getSecurityContext();

            if ( isThereAnAllowedRoleInSecurityContext(rolesAllowedOnClass, secContext)) {
                logger.info(CLASS_ROLES_ALLOWED_ALLOW);
                return;
            }

            logger.info(CLASS_ROLES_ALLOWED_DENY);
            throw new ForbiddenException(CLASS_ROLES_ALLOWED_DENY);
        }

        if ( resourceClass.isAnnotationPresent(PermitAll.class)) {
            logger.info(CLASS_PERMIT_ALL);
            return;
        }

        // -- Method
        if ( resourceMethod.isAnnotationPresent(DenyAll.class)) {
            logger.info(METHOD_DENY_ALL);
            throw new ForbiddenException(METHOD_DENY_ALL);
        }

        if ( resourceMethod.isAnnotationPresent(RolesAllowed.class)) {
            final String[] rolesAllowedOnMethod = resourceMethod.getAnnotation(RolesAllowed.class).value();
            final SecurityContext secContext    = containerRequestContext.getSecurityContext();

            if ( isThereAnAllowedRoleInSecurityContext(rolesAllowedOnMethod, secContext)) {
                logger.info(METHOD_ROLES_ALLOWED_ALLOW);
                return;
            }

            logger.info(METHOD_ROLES_ALLOWED_DENY);
            throw new ForbiddenException(METHOD_ROLES_ALLOWED_DENY);
        }

        if ( resourceMethod.isAnnotationPresent(PermitAll.class)) {
            logger.info(METHOD_PERMIT_ALL);
            return;
        }

        // -- Default
        logger.info(ALL_EXHAUSTED_DENY);
        throw new ForbiddenException(ALL_EXHAUSTED_DENY);
    }
}
