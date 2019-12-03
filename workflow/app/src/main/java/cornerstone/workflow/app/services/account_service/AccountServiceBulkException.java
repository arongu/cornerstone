package cornerstone.workflow.app.services.account_service;

import java.util.LinkedList;
import java.util.List;

public class AccountServiceBulkException extends Exception {
    private final List<AccountServiceException> exceptions;

    public AccountServiceBulkException() {
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
