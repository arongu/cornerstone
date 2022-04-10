package cornerstone.webapp.services.accounts.management.exceptions;

import cornerstone.webapp.services.accounts.management.exceptions.account.single.AccountDeletionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeletionExceptionTest {
    @Test
    public void getMessage() {
        final AccountDeletionException deletionException = new AccountDeletionException("delete@mail.com");

        assertEquals("Failed to delete 'delete@mail.com'.", deletionException.getMessage());
    }
}
