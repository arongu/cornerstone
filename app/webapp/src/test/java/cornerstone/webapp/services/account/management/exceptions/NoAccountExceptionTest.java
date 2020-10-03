package cornerstone.webapp.services.account.management.exceptions;

import cornerstone.webapp.services.account.management.exceptions.single.NoAccountException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NoAccountExceptionTest {
    @Test
    void getMessage() {
        final NoAccountException noAccountException = new NoAccountException("apples@yahoo.com");

        assertEquals("Account 'apples@yahoo.com' does not exist.", noAccountException.getMessage());
    }
}
