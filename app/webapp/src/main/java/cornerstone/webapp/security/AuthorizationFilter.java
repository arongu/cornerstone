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

    private static final String NO_ANNOTATIONS             = "No annotation present on class or methods, nothing to work with. (Denied)" ;
    private static final String CLASS_DENY_ALL             = "@DenyAll on class. (Denied)" ;
    private static final String CLASS_ROLES_ALLOWED_DENY   = "@RolesAllowed on class, but user is not in the allowed roles. (Denied)" ;
    private static final String CLASS_ROLES_ALLOWED_ALLOW  = "@RolesAllowed, user is in the allowed roles. (Allowed)" ;
    private static final String CLASS_PERMIT_ALL           = "@PermitAll on class. (Allowed)" ;

    private static final String METHOD_NO_ANNOTATIONS      = "Class level annotations exhausted, and no method level annotation is present.";
    private static final String METHOD_DENY_ALL            = "@DenyAll on method. (Denied)" ;
    private static final String METHOD_ROLES_ALLOWED_DENY  = "@RolesAllowed on method, but user is not in the allowed roles. (Denied)" ;
    private static final String METHOD_ROLES_ALLOWED_ALLOW = "@RolesAllowed, user is in the allowed roles. (Allowed)" ;
    private static final String METHOD_PERMIT_ALL          = "@PermitAll on class. (Allowed)" ;

    private static final String ALL_EXHAUSTED_DENY         = "All method and class level rules are exhausted, denying by default. (Denied)" ;

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) throws RuntimeException {
        final Class<?> resourceClass = resourceInfo.getResourceClass();
        final Method resourceMethod  = resourceInfo.getResourceMethod();

        // Deny if no class or method level annotations are present.
        if ( resourceClass.getAnnotations().length == 0 && resourceMethod.getAnnotations().length == 0) {
            logger.info(NO_ANNOTATIONS);
            throw new ForbiddenException(NO_ANNOTATIONS);
        }

        // -- Class ---------------------------------------------------------------------------------------------
        if ( resourceClass.isAnnotationPresent(DenyAll.class)) {
            logger.info(CLASS_DENY_ALL);
            throw new ForbiddenException(CLASS_DENY_ALL);
        }

        if (resourceClass.isAnnotationPresent(RolesAllowed.class)) {
            final String[] classLevelRolesAllowed = resourceClass.getAnnotation(RolesAllowed.class).value();

            if ( classLevelRolesAllowed.length > 0) {
                final SecurityContext secContext = containerRequestContext.getSecurityContext();

                for ( final String r : classLevelRolesAllowed) {
                    if ( secContext.isUserInRole(r)) {
                        logger.info(CLASS_ROLES_ALLOWED_ALLOW);
                        return;
                    }
                }
            }

            logger.info(CLASS_ROLES_ALLOWED_DENY);
            throw new ForbiddenException(CLASS_ROLES_ALLOWED_DENY);
        }

        if ( resourceClass.isAnnotationPresent(PermitAll.class)){
            logger.info(CLASS_PERMIT_ALL);
            return;
        }

        // -- Method ---------------------------------------------------------------------------------------------
        if (resourceMethod.getAnnotations().length == 0) {
            logger.info(METHOD_NO_ANNOTATIONS);
            throw new ForbiddenException(METHOD_NO_ANNOTATIONS);
        }

        if ( resourceMethod.isAnnotationPresent(DenyAll.class)) {
            logger.info(METHOD_DENY_ALL);
            throw new ForbiddenException(METHOD_DENY_ALL);
        }

        if ( resourceMethod.isAnnotationPresent(RolesAllowed.class)) {
            final String[] methodLevelRolesAllowed = resourceMethod.getAnnotation(RolesAllowed.class).value();

            if ( methodLevelRolesAllowed.length > 0) {
                final SecurityContext secContext = containerRequestContext.getSecurityContext();

                for ( final String r : methodLevelRolesAllowed) {
                    if ( secContext.isUserInRole(r)) {
                        logger.info(METHOD_ROLES_ALLOWED_ALLOW);
                        return;
                    }
                }
            }

            logger.info(METHOD_ROLES_ALLOWED_DENY);
            throw new ForbiddenException(METHOD_ROLES_ALLOWED_DENY);
        }

        if ( resourceMethod.isAnnotationPresent(PermitAll.class)){
            logger.info(METHOD_PERMIT_ALL);
            return;
        }

        logger.info(ALL_EXHAUSTED_DENY);
        throw new ForbiddenException(ALL_EXHAUSTED_DENY);
    }
}
