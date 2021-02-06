package cornerstone.webapp.services.accounts.management.exceptions;

import cornerstone.webapp.services.accounts.management.exceptions.single.LockUpdateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LockUpdateExceptionTest {
    @Test
    public void getMessage() {
        final LockUpdateException lockUpdateException = new LockUpdateException("mail@xmail.com");

        assertEquals("Failed to update lock for 'mail@xmail.com'.", lockUpdateException.getMessage());
    }
}
