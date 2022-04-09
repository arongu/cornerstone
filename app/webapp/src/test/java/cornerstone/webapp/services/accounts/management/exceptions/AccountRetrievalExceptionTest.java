package cornerstone.webapp.services.accounts.management.exceptions;

import cornerstone.webapp.services.accounts.management.exceptions.account.single.AccountRetrievalException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountRetrievalExceptionTest {
    @Test
    public void getMessage() {
        final AccountRetrievalException accountRetrievalException = new AccountRetrievalException("user@xmail.com");

        assertEquals("Failed to retrieve 'user@xmail.com'.", accountRetrievalException.getMessage());
    }
}
