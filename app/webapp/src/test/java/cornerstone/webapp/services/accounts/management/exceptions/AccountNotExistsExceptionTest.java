package cornerstone.webapp.services.accounts.management.exceptions;

import cornerstone.webapp.services.accounts.management.exceptions.account.single.AccountNotExistsException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountNotExistsExceptionTest {
    @Test
    void getMessage() {
        final AccountNotExistsException accountNotExistsException = new AccountNotExistsException("apples@yahoo.com");

        assertEquals("Account 'apples@yahoo.com' does not exist.", accountNotExistsException.getMessage());
    }
}
