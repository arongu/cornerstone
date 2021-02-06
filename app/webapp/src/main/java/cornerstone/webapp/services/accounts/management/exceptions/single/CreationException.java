package cornerstone.webapp.services.accounts.management.exceptions.single;

public class CreationException extends Exception {
    public static final String EXCEPTION_MESSAGE_ACCOUNT_CREATION_FAILED = "Failed to create '%s'.";

    public CreationException(final String email) {
        super(String.format(EXCEPTION_MESSAGE_ACCOUNT_CREATION_FAILED, email));
    }
}
