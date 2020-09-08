package cornerstone.webapp.services.account.administration.exceptions.bulk;

import java.util.LinkedList;
import java.util.List;

public class PartialCreationException extends Exception {
    private final List<String> exceptionMessages;

    public PartialCreationException() {
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
