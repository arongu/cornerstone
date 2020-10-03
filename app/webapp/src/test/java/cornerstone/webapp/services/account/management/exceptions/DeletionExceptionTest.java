package cornerstone.webapp.services.account.management.exceptions;

import cornerstone.webapp.services.account.management.exceptions.single.DeletionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeletionExceptionTest {
    @Test
    public void getMessage() {
        final DeletionException deletionException = new DeletionException("delete@mail.com");

        assertEquals("Failed to delete 'delete@mail.com'.", deletionException.getMessage());
    }
}
