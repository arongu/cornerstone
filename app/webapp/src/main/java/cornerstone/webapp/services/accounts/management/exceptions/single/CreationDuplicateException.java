package cornerstone.webapp.services.accounts.management.exceptions.single;

public class CreationDuplicateException extends Exception {
    public static final String EXCEPTION_MESSAGE_ACCOUNT_CREATION_ALREADY_EXISTS = "Failed to create '%s' (already exists).";

    public CreationDuplicateException(final String email) {
        super(String.format(EXCEPTION_MESSAGE_ACCOUNT_CREATION_ALREADY_EXISTS, email));
    }
}
