package cornerstone.webapp.services.accounts.management.exceptions;

import cornerstone.webapp.services.accounts.management.exceptions.account.single.PasswordUpdateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordUpdateExceptionTest {
    @Test
    public void getMessage() {
        final PasswordUpdateException passwordUpdateException = new PasswordUpdateException("micimacko@mail.com");

        assertEquals("Failed to update password for 'micimacko@mail.com'.", passwordUpdateException.getMessage());
    }
}
