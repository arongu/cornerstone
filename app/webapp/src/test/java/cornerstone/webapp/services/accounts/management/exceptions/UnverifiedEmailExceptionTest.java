package cornerstone.webapp.services.accounts.management.exceptions;

import cornerstone.webapp.services.accounts.management.exceptions.account.single.UnverifiedEmailException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnverifiedEmailExceptionTest {
    @Test
    public void getMessage() {
        final UnverifiedEmailException unverifiedEmailException = new UnverifiedEmailException("mymail@x.com");

        assertEquals("Account is not verified 'mymail@x.com'.", unverifiedEmailException.getMessage());
    }
}

