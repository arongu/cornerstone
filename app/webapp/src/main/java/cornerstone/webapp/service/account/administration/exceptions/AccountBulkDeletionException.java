package cornerstone.webapp.service.account.administration.exceptions;

import java.util.LinkedList;
import java.util.List;

public class AccountBulkDeletionException extends Exception {
    private final List<String> exceptionMessages;

    public AccountBulkDeletionException() {
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
