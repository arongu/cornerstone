package cornerstone.webapp.services.account.administration;

import java.util.LinkedList;
import java.util.List;

public class AccountManagerMultipleException extends Exception {
    private final List<AccountManagerException> exceptions;

    public AccountManagerMultipleException() {
        super();
        exceptions = new LinkedList<>();
    }

    public List<AccountManagerException> getExceptions() {
        return exceptions;
    }

    public void addException(final AccountManagerException accountManagerException) {
        exceptions.add(accountManagerException);
    }
}
