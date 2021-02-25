package cornerstone.webapp.services.accounts.management;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserRoleTest {
    @Test
    public void valueOf() {
        assertEquals(UserRole.NO_ROLE, UserRole.valueOf("NO_ROLE"));
        assertEquals(UserRole.USER,    UserRole.valueOf("USER"));
        assertEquals(UserRole.SUPER,   UserRole.valueOf("SUPER"));
        assertEquals(UserRole.ADMIN,   UserRole.valueOf("ADMIN"));
    }

    @Test
    public void getId() {
        assertEquals(0, UserRole.valueOf("NO_ROLE").getId());
        assertEquals(1, UserRole.valueOf("USER").getId());
        assertEquals(5, UserRole.valueOf("SUPER").getId());
        assertEquals(7, UserRole.valueOf("ADMIN").getId());
        assertThrows(IllegalArgumentException.class, () -> UserRole.valueOf("HAHAHA"));
    }
}
