package cornerstone.webapp.security;

import cornerstone.webapp.services.accounts.management.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.SecurityContext;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class AuthorizationFilterRolesAllowedTest {
    @RolesAllowed({"ADMIN"})
    @PermitAll
    public static class FilterMe {
        @DenyAll
        public void toBeFiltered() {
        }
    }

    @Test
    void filter_shouldIgnorePermitAllAndShouldNotThrowException_whenRolesAllowedAnnotationIsSetToAdminAndUserRolesContainsAdmin() throws NoSuchMethodException {
        // method, class initialization to avoid NullPointerException, for some reason it fails when Mockito does it
        final Class clazz   = FilterMe.class;
        final Method method = FilterMe.class.getMethod("toBeFiltered");
        // mocking ResourceInfo
        final ResourceInfo resourceInfo = Mockito.mock(ResourceInfo.class);
        Mockito.when(resourceInfo.getResourceClass()).thenReturn(clazz);
        Mockito.when(resourceInfo.getResourceMethod()).thenReturn(method);

        // prepare security context
        final boolean isSecure        = false;
        final Claims claims           = null;
        final Principal principal     = new PrincipalImpl(UserRole.ADMIN.name());
        final Set<UserRole> userRoles = new HashSet<>();
        userRoles.add(UserRole.ADMIN);

        // SecurityContext, ContainerRequestContext
        final ContainerRequestContext containerRequestContext = Mockito.mock(ContainerRequestContext.class);
        final SecurityContext securityContext                 = new JwtSecurityContext(isSecure, principal, userRoles, claims);
        Mockito.when(containerRequestContext.getSecurityContext()).thenReturn(securityContext);


        assertDoesNotThrow(() -> {
            final AuthorizationFilter authorizationFilter = new AuthorizationFilter(resourceInfo);
            authorizationFilter.filter(containerRequestContext);
        });
    }
}
