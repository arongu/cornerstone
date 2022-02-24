package cornerstone.webapp.services.accounts.management.exceptions.account.single;

public class DeletionException extends Exception {
    public static final String EXCEPTION_MESSAGE_ACCOUNT_DELETION_FAILED = "Failed to delete '%s'.";

    public DeletionException(final String email) {
        super(String.format(EXCEPTION_MESSAGE_ACCOUNT_DELETION_FAILED, email));
    }
}
