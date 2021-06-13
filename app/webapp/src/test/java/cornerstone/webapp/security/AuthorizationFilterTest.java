package cornerstone.webapp.security;

/*
@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class UserServiceUnitTest {

    UserService userService;

... //
}
 */


import cornerstone.webapp.services.accounts.management.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class AuthorizationFilterTest {
    private static ResourceInfo resourceInfo;

    // annotations through visualization :)
    @PermitAll
    public static class FilterMe {
        private int called = 0;

        @DenyAll
        public void run() {
            called++;
        }

        public int getCalled() {
            return called;
        }
    }

    @BeforeAll
    public static void beforeAll() throws NoSuchMethodException {
        resourceInfo = Mockito.mock(ResourceInfo.class);
        Mockito.when(resourceInfo.getResourceMethod()).thenReturn(FilterMe.class.getMethod("run"));
        Mockito.when(resourceInfo.getResourceClass().getAnnotations()).thenReturn(FilterMe.class.getAnnotations());
    }

    @Test
    void test() {
        // prepare security context
        final boolean isSecure                = false;
        final Claims claims                   = null;
        final Principal principal             = new PrincipalImpl("Legion");
        final Set<UserRole> userRoles         = new HashSet<>();
        userRoles.add(UserRole.USER);
        userRoles.add(UserRole.ADMIN);
        final SecurityContext securityContext = new JwtSecurityContext(isSecure, principal, userRoles, claims);
        // add security context to container request context
        ContainerRequestContext containerRequestContext = Mockito.mock(ContainerRequestContext.class);
        Mockito.when(containerRequestContext.getSecurityContext()).thenReturn(securityContext);


        AuthorizationFilter authorizationFilter = new AuthorizationFilter(resourceInfo);
        authorizationFilter.filter(containerRequestContext);

    }

}
