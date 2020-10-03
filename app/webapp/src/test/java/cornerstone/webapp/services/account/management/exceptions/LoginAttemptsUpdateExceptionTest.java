package cornerstone.webapp.services.account.management.exceptions;

import cornerstone.webapp.services.account.management.exceptions.single.LoginAttemptsUpdateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoginAttemptsUpdateExceptionTest {
    @Test
    public void getMessage() {
        final LoginAttemptsUpdateException loginAttemptsUpdateException = new LoginAttemptsUpdateException("mymail@x.com");

        assertEquals("Failed to update login attempts for 'mymail@x.com'.", loginAttemptsUpdateException.getMessage());
    }
}
