package cornerstone.webapp.services.account.administration.exceptions.multi;

import java.util.LinkedList;
import java.util.List;

public class MultiDeletionException extends Exception {
    private final List<String> exceptionMessages;

    public MultiDeletionException() {
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
