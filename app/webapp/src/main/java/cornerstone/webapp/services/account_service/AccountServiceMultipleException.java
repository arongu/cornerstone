package cornerstone.webapp.services.account_service;

import java.util.LinkedList;
import java.util.List;

public class AccountServiceMultipleException extends Exception {
    private final List<AccountServiceException> exceptions;

    public AccountServiceMultipleException() {
        super();
        exceptions = new LinkedList<>();
    }

    public List<AccountServiceException> getExceptions() {
        return exceptions;
    }

    public void addException(final AccountServiceException accountServiceException) {
        exceptions.add(accountServiceException);
    }
}
