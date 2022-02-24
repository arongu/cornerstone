package cornerstone.webapp.services.accounts.management.exceptions;

import cornerstone.webapp.services.accounts.management.exceptions.account.single.DeletionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeletionExceptionTest {
    @Test
    public void getMessage() {
        final DeletionException deletionException = new DeletionException("delete@mail.com");

        assertEquals("Failed to delete 'delete@mail.com'.", deletionException.getMessage());
    }
}
