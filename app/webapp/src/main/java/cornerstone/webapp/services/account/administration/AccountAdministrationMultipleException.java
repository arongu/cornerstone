package cornerstone.webapp.services.account.administration;

import java.util.LinkedList;
import java.util.List;

public class AccountAdministrationMultipleException extends Exception {
    private final List<AccountAdministrationException> exceptions;

    public AccountAdministrationMultipleException() {
        super();
        exceptions = new LinkedList<>();
    }

    public List<AccountAdministrationException> getExceptions() {
        return exceptions;
    }

    public void addException(final AccountAdministrationException accountAdministrationException) {
        exceptions.add(accountAdministrationException);
    }
}
