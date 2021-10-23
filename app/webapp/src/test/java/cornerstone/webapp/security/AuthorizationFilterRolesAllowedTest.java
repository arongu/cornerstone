package cornerstone.webapp.security;

import cornerstone.webapp.services.accounts.management.enums.SYSTEM_ROLE_ENUM;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.SecurityContext;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class AuthorizationFilterRolesAllowedTest {
    private static Class clazz;
    private static Method method;

    // test class annotations
    @RolesAllowed({"ADMIN"})
    @PermitAll
    public static class FilterMe {
        @PermitAll
        public void toBeFiltered() {
        }
    }


    @BeforeAll
    public static void beforeAll() throws NoSuchMethodException {
        clazz  = FilterMe.class;
        method = FilterMe.class.getMethod("toBeFiltered");
    }

    @Test
    void CLASS_LEVEL__filter__RolesAllowedAdmin_shouldNotThrowExceptionWhenUserInAdminRole() {
        final ResourceInfo resourceInfo = Mockito.mock(ResourceInfo.class);
        Mockito.when(resourceInfo.getResourceClass()).thenReturn(clazz);
        Mockito.when(resourceInfo.getResourceMethod()).thenReturn(method);

        // prepare security context
        final boolean isSecure        = false;
        final Claims claims           = null;
        final Principal principal     = new PrincipalImpl("DrDummy");
        final Set<SYSTEM_ROLE_ENUM> roles = new HashSet<>();
        roles.add(SYSTEM_ROLE_ENUM.ADMIN);

        // SecurityContext, ContainerRequestContext
        final ContainerRequestContext containerRequestContext = Mockito.mock(ContainerRequestContext.class);
        final SecurityContext securityContext                 = new JwtSecurityContext(isSecure, principal, roles, claims);
        Mockito.when(containerRequestContext.getSecurityContext()).thenReturn(securityContext);


        assertDoesNotThrow(() -> {
            final AuthorizationFilter authorizationFilter = new AuthorizationFilter(resourceInfo);
            authorizationFilter.filter(containerRequestContext);
        });
    }

    @Test
    void filter_shouldIgnorePermitAllAndMethodLevelDenyAllAndShouldThrowForbiddenException_whenRolesAllowedAnnotationIsSetToAdminAndUserRoleDoesNotContainAdmin() {
        final ResourceInfo resourceInfo = Mockito.mock(ResourceInfo.class);
        Mockito.when(resourceInfo.getResourceClass()).thenReturn(clazz);
        Mockito.when(resourceInfo.getResourceMethod()).thenReturn(method);

        // prepare security context
        final boolean isSecure        = false;
        final Claims claims           = null;
        final Principal principal     = new PrincipalImpl("DrDummy");
        final Set<SYSTEM_ROLE_ENUM> roles = new HashSet<>();
        roles.add(SYSTEM_ROLE_ENUM.USER);

        // SecurityContext, ContainerRequestContext
        final ContainerRequestContext containerRequestContext = Mockito.mock(ContainerRequestContext.class);
        final SecurityContext securityContext                 = new JwtSecurityContext(isSecure, principal, roles, claims);
        Mockito.when(containerRequestContext.getSecurityContext()).thenReturn(securityContext);


        assertThrows(ForbiddenException.class, () -> {
            final AuthorizationFilter authorizationFilter = new AuthorizationFilter(resourceInfo);
            authorizationFilter.filter(containerRequestContext);
        });
    }
}
