package cornerstone.workflow.app.services.account_service;

import java.util.LinkedList;
import java.util.List;

public class AccountCrudServiceBulkException extends Exception {
    private final List<AccountCrudServiceException> exceptions;

    public AccountCrudServiceBulkException() {
        super();
        exceptions = new LinkedList<>();
    }

    public List<AccountCrudServiceException> getExceptions() {
        return exceptions;
    }

    public void addException(final AccountCrudServiceException accountCrudServiceException) {
        exceptions.add(accountCrudServiceException);
    }
}
