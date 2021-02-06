package cornerstone.webapp.services.accounts.management.exceptions;

import cornerstone.webapp.services.accounts.management.exceptions.single.RetrievalException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RetrievalExceptionTest {
    @Test
    public void getMessage() {
        final RetrievalException retrievalException = new RetrievalException("user@xmail.com");

        assertEquals("Failed to retrieve 'user@xmail.com'.", retrievalException.getMessage());
    }
}
