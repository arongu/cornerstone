package cornerstone.webapp.services.accounts.management.exceptions.multi;

import java.util.LinkedList;
import java.util.List;

public class MultiCreationException extends Exception {
    private final List<String> exceptionMessages;

    public MultiCreationException() {
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
