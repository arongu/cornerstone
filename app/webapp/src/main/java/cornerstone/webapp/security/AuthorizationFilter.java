package cornerstone.webapp.security;

import cornerstone.webapp.logmsg.CommonLogMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.Arrays;

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
    private static final String NO_ANNOTATIONS             = "DENIED - %s -  No annotations are present on class or methods.";
    //private static final String NO_ANNOTATIONS_2           = "requester:xmail.com src:127.0.0.1:4212 method:GET, uri:/live/keys, access:DENIED, reason:No annotations are present on class or methods.";
    // Class
    private static final String CLASS_DENY_ALL             = "DENIED - %s - @DenyAll (CLASS)";
    private static final String CLASS_ROLES_ALLOWED_DENY   = "DENIED - %s -  @RolesAllowed (CLASS): %s, user is not in the allowed roles.";
    private static final String CLASS_ROLES_ALLOWED_ALLOW  = "ALLOWED - %s - @RolesAllowed (CLASS): %s.";
    private static final String CLASS_PERMIT_ALL           = "ALLOWED - %s - @PermitAll (CLASS)";
    // Method
    private static final String METHOD_DENY_ALL            = "DENIED - %s - @DenyAll (METHOD)";
    private static final String METHOD_ROLES_ALLOWED_DENY  = "DENIED - %s - @RolesAllowed (METHOD): %s, user is not in the allowed roles.";
    private static final String METHOD_ROLES_ALLOWED_ALLOW = "ALLOWED - %s - @RolesAllowed (METHOD): %s, user is in the allowed roles.";
    private static final String METHOD_PERMIT_ALL          = "ALLOWED - %s - @PermitAll (METHOD)";
    // Default
    private static final String ALL_EXHAUSTED_DENY         = "DENIED - %s - All class and method level rules are exhausted.";

    private static boolean isThereAnAllowedRoleInSecurityContext(final String[] rolesFromAnnotation, final SecurityContext securityContext) {
        if ( securityContext != null && rolesFromAnnotation != null && rolesFromAnnotation.length > 0) {
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

    @Context
    private HttpServletRequest httpServletRequest;


    public AuthorizationFilter() {
    }

    public AuthorizationFilter(final ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

    public AuthorizationFilter(final ResourceInfo resourceInfo, final HttpServletRequest httpServletRequest) {
        this.resourceInfo       = resourceInfo;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) throws RuntimeException {
        // forbid when not initialized
        if ( resourceInfo == null || containerRequestContext == null) {
            final String m = CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + "resourceInfo/containerRequest is not set!";
            logger.error(m);
            throw new ForbiddenException();
        }

        // forbid when class and method information null
        final Class<?> clazz = resourceInfo.getResourceClass();
        final Method method  = resourceInfo.getResourceMethod();
        if ( clazz == null || method == null ) {
            final String msgClazzMethod = CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + "resourceInfo.getResourceClass()/resourceInfo.getResourceMethod() returned null!";
            logger.error(msgClazzMethod);
            throw new ForbiddenException();
        }

        // get security context and set user name
        final SecurityContext securityContext = containerRequestContext.getSecurityContext();
        final String userName                 = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "Anonymous";

        // -- Early deny, if no annotation is present.
        if (clazz.getAnnotations().length == 0 && method.getAnnotations().length == 0) {
            final String msgAnno = CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + String.format(NO_ANNOTATIONS, userName);
            logger.info(msgAnno);
            throw new ForbiddenException();
        }

        // -- Class
        if ( clazz.isAnnotationPresent(DenyAll.class)) {
            final String msgDenyAll = CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + String.format(CLASS_DENY_ALL, userName);
            logger.info(msgDenyAll);
            throw new ForbiddenException();
        }

        if ( clazz.isAnnotationPresent(RolesAllowed.class)) {
            final String[] rolesAllowedOnClass = clazz.getAnnotation(RolesAllowed.class).value();

            if ( isThereAnAllowedRoleInSecurityContext(rolesAllowedOnClass, securityContext)) {
                final String msgRolesAllowed = CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + String.format(CLASS_ROLES_ALLOWED_ALLOW, userName, Arrays.toString(rolesAllowedOnClass));
                logger.info(msgRolesAllowed);
                return;
            }

            final String msgClassRolesDeny = CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + String.format(CLASS_ROLES_ALLOWED_DENY, userName, Arrays.toString(rolesAllowedOnClass));
            logger.info(msgClassRolesDeny);
            throw new ForbiddenException();
        }

        if ( clazz.isAnnotationPresent(PermitAll.class)) {
            final String msgClassPermitAll = CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + String.format(CLASS_PERMIT_ALL, userName);
            logger.info(msgClassPermitAll);
            return;
        }

        // -- Method
        if ( method.isAnnotationPresent(DenyAll.class)) {
            final String msgMethodDenyAll = CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + String.format(METHOD_DENY_ALL, userName);
            logger.info(msgMethodDenyAll);
            throw new ForbiddenException();
        }

        if ( method.isAnnotationPresent(RolesAllowed.class)) {
            final String[] rolesAllowedOnMethod = method.getAnnotation(RolesAllowed.class).value();
            final SecurityContext secContext    = containerRequestContext.getSecurityContext();

            if ( isThereAnAllowedRoleInSecurityContext(rolesAllowedOnMethod, secContext)) {
                final String msgMethodRolesAllow = CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + String.format(METHOD_ROLES_ALLOWED_ALLOW, userName, Arrays.toString(rolesAllowedOnMethod));
                logger.info(msgMethodRolesAllow);
                return;
            }

            final String msgMethodRolesDeny = CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + String.format(METHOD_ROLES_ALLOWED_DENY, userName, Arrays.toString(rolesAllowedOnMethod));
            logger.info(msgMethodRolesDeny);
            throw new ForbiddenException();
        }

        if ( method.isAnnotationPresent(PermitAll.class)) {
            final String msgMethodPermitAll = CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + String.format(METHOD_PERMIT_ALL, userName);
            logger.info(msgMethodPermitAll);
            return;
        }

        // -- Default
        final String msgDefaultRule = CommonLogMessages.GENRE_SECURITY + CommonLogMessages.GENRE_FILTER + String.format(ALL_EXHAUSTED_DENY, userName);
        logger.info(msgDefaultRule);
        throw new ForbiddenException();
    }
}
