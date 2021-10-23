package cornerstone.webapp.services.accounts.management.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RolesTest {
    @Test
    public void valueOf() {
        assertEquals(SYSTEM_ROLE_ENUM.NONE, SYSTEM_ROLE_ENUM.valueOf("NONE"));
        assertEquals(SYSTEM_ROLE_ENUM.USER,    SYSTEM_ROLE_ENUM.valueOf("USER"));
        assertEquals(SYSTEM_ROLE_ENUM.ADMIN,   SYSTEM_ROLE_ENUM.valueOf("ADMIN"));
    }

    @Test
    public void getId() {
        assertEquals(0, SYSTEM_ROLE_ENUM.valueOf("NONE").getId());
        assertEquals(1, SYSTEM_ROLE_ENUM.valueOf("USER").getId());
        assertEquals(2, SYSTEM_ROLE_ENUM.valueOf("ADMIN").getId());
        assertThrows(IllegalArgumentException.class, () -> SYSTEM_ROLE_ENUM.valueOf("HAHAHA"));
    }
}
