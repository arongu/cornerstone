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

/*
!!! IF CLASS LEVEL ANNOTATION IS SET, ALL METHOD LEVEL ANNOTATIONS ARE IGNORED !!!
Order of strength from top to bottom, lines on top are stronger than lines on bottom.

------------------------------------------------------------------------------------------------------------------------
Class
------------------------------------------------------------------------------------------------------------------------
ANNOTATION       EFFECT        IMPLEMENTATION NOTE
------------------------------------------------------------------------------------------------------------------------
no annotation    DENY ALL      DEFAULT            :

@DenyAll         DENY ALL      IF SET STOP/IGNORE : Class level @RolesAllowed, @PermitAll, all method level annotations
@RolesAllowed    ALLOW ROLES   IF SET STOP/IGNORE : Class level @PermitAll               , all method level annotations
@PermitAll       PERMIT ALL    IF SET STOP/IGNORE :                                      , all method level annotations

------------------------------------------------------------------------------------------------------------------------
Method
------------------------------------------------------------------------------------------------------------------------
ANNOTATION       EFFECT        IMPLEMENTATION NOTE
------------------------------------------------------------------------------------------------------------------------
no annotation    DENY ALL      DEFAULT            :

@DenyAll         DENY ALL      IF SET STOP/IGNORE : Method level @RolesAllowed, @PermitAll
@RolesAllowed    ALLOW ROLES   IF SET STOP/IGNORE : Method level @PermitAll
@PermitAll       PERMIT ALL    IF SET STOP/IGNORE :

 */

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

    public AuthorizationFilter() {
    }

    public AuthorizationFilter(final ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) throws RuntimeException {
        final Class<?> clazz = resourceInfo.getResourceClass();
        final Method method  = resourceInfo.getResourceMethod();

        // -- Early deny, if no annotation is present.
        if ( clazz.getAnnotations().length == 0 && method.getAnnotations().length == 0) {
            logger.info(NO_ANNOTATIONS);
            throw new ForbiddenException(NO_ANNOTATIONS);
        }

        // -- Class
        if ( clazz.isAnnotationPresent(DenyAll.class)) {
            logger.info(CLASS_DENY_ALL);
            throw new ForbiddenException(CLASS_DENY_ALL);
        }

        if ( clazz.isAnnotationPresent(RolesAllowed.class)) {
            final String[] rolesAllowedOnClass = clazz.getAnnotation(RolesAllowed.class).value();
            final SecurityContext secContext   = containerRequestContext.getSecurityContext();

            if ( isThereAnAllowedRoleInSecurityContext(rolesAllowedOnClass, secContext)) {
                logger.info(CLASS_ROLES_ALLOWED_ALLOW);
                return;
            }

            logger.info(CLASS_ROLES_ALLOWED_DENY);
            throw new ForbiddenException(CLASS_ROLES_ALLOWED_DENY);
        }

        if ( clazz.isAnnotationPresent(PermitAll.class)) {
            logger.info(CLASS_PERMIT_ALL);
            return;
        }

        // -- Method
        if ( method.isAnnotationPresent(DenyAll.class)) {
            logger.info(METHOD_DENY_ALL);
            throw new ForbiddenException(METHOD_DENY_ALL);
        }

        if ( method.isAnnotationPresent(RolesAllowed.class)) {
            final String[] rolesAllowedOnMethod = method.getAnnotation(RolesAllowed.class).value();
            final SecurityContext secContext    = containerRequestContext.getSecurityContext();

            if ( isThereAnAllowedRoleInSecurityContext(rolesAllowedOnMethod, secContext)) {
                logger.info(METHOD_ROLES_ALLOWED_ALLOW);
                return;
            }

            logger.info(METHOD_ROLES_ALLOWED_DENY);
            throw new ForbiddenException(METHOD_ROLES_ALLOWED_DENY);
        }

        if ( method.isAnnotationPresent(PermitAll.class)) {
            logger.info(METHOD_PERMIT_ALL);
            return;
        }

        // -- Default
        logger.info(ALL_EXHAUSTED_DENY);
        throw new ForbiddenException(ALL_EXHAUSTED_DENY);
    }
}
