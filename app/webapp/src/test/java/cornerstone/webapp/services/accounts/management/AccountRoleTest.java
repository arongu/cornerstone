package cornerstone.webapp.services.accounts.management;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AccountRoleTest {
    @Test
    public void valueOf() {
        assertEquals(AccountRole.NO_ROLE, AccountRole.valueOf("NO_ROLE"));
        assertEquals(AccountRole.USER,    AccountRole.valueOf("USER"));
        assertEquals(AccountRole.SUPER,   AccountRole.valueOf("SUPER"));
        assertEquals(AccountRole.ADMIN,   AccountRole.valueOf("ADMIN"));
    }

    @Test
    public void getId() {
        assertEquals(0, AccountRole.valueOf("NO_ROLE").getId());
        assertEquals(1, AccountRole.valueOf("USER").getId());
        assertEquals(5, AccountRole.valueOf("SUPER").getId());
        assertEquals(7, AccountRole.valueOf("ADMIN").getId());
        assertThrows(IllegalArgumentException.class, () -> AccountRole.valueOf("HAHAHA"));
    }
}
