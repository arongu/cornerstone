package cornerstone.webapp.services.accounts.management.exceptions.single;

public class LockUpdateException extends Exception {
    public static final String EXCEPTION_MESSAGE_UPDATE_LOCK_FAILED = "Failed to update lock for '%s'.";

    public LockUpdateException(final String email) {
        super(String.format(EXCEPTION_MESSAGE_UPDATE_LOCK_FAILED, email));
    }
}
