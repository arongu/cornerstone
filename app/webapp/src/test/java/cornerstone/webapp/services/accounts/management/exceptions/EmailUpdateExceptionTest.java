package cornerstone.webapp.services.accounts.management.exceptions;

import cornerstone.webapp.services.accounts.management.exceptions.single.EmailUpdateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmailUpdateExceptionTest {
    @Test
    public void getMessage() {
        final EmailUpdateException emailUpdateException = new EmailUpdateException("email@mail.com" , "newemail@mail.com");

        assertEquals("Failed to update email for 'email@mail.com' -> 'newemail@mail.com'.", emailUpdateException.getMessage());
    }
}
