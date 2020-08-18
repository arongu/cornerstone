package cornerstone.webapp.service.account.administration.exceptions;

import java.util.LinkedList;
import java.util.List;

public class AccountManagerSqlBulkException extends Exception {
    private final List<String> exceptionMessages;

    public AccountManagerSqlBulkException() {
        super();
        exceptionMessages = new LinkedList<>();
    }

    public List<String> getExceptionMessages() {
        return exceptionMessages;
    }

    public void addExceptionMessage(final String exceptionMessage) {
        exceptionMessages.add(exceptionMessage);
    }
}
