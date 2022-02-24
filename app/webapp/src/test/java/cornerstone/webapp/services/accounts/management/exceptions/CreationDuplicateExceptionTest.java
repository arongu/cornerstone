package cornerstone.webapp.services.accounts.management.exceptions;

import cornerstone.webapp.services.accounts.management.exceptions.account.single.CreationDuplicateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreationDuplicateExceptionTest {
    @Test
    public void getMessage() {
        final CreationDuplicateException creationDuplicateException = new CreationDuplicateException("foxy@gmail.com");

        assertEquals("Failed to create 'foxy@gmail.com' (already exists).", creationDuplicateException.getMessage());
    }
}
