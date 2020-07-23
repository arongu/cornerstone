package cornerstone.webapp.services.account.administration;

import java.util.LinkedList;
import java.util.List;

public class AccountAdminMultipleException extends Exception {
    private final List<AccountAdminException> exceptions;

    public AccountAdminMultipleException() {
        super();
        exceptions = new LinkedList<>();
    }

    public List<AccountAdminException> getExceptions() {
        return exceptions;
    }

    public void addException(final AccountAdminException accountAdminException) {
        exceptions.add(accountAdminException);
    }
}
