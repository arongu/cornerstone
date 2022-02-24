package cornerstone.webapp.services.accounts.management.exceptions;

import cornerstone.webapp.services.accounts.management.exceptions.account.single.CreationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreationExceptionTest {
    @Test
    public void getMessage() {
        final CreationException creationException = new CreationException("foxy@gmail.com");

        assertEquals("Failed to create 'foxy@gmail.com'.", creationException.getMessage());
    }
}
