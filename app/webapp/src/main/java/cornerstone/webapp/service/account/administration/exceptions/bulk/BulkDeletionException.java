package cornerstone.webapp.service.account.administration.exceptions.bulk;

import java.util.LinkedList;
import java.util.List;

public class BulkDeletionException extends Exception {
    private final List<String> exceptionMessages;

    public BulkDeletionException() {
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
