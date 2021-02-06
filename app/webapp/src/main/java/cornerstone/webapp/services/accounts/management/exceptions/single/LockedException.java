package cornerstone.webapp.services.accounts.management.exceptions.single;

public class LockedException extends Exception {
    public static final String EXCEPTION_MESSAGE_ACCOUNT_IS_LOCKED = "Account is locked '%s'.";

    public LockedException(final String email) {
        super(String.format(EXCEPTION_MESSAGE_ACCOUNT_IS_LOCKED, email));
    }
}
